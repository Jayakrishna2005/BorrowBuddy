from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.shortcuts import get_object_or_404
from .models import User, Item, Booking, Category, Message
from .serializers import UserSerializer, ItemSerializer, BookingSerializer, CategorySerializer, MessageSerializer, ReviewSerializer

class AuthLoginView(APIView):
    def post(self, request):
        name = request.data.get('name')
        reg_number = request.data.get('regNumber')
        email = request.data.get('email')

        try:
            user = User.objects.get(registration_number=reg_number)
            if user.email.lower() == email.lower():
                serializer = UserSerializer(user)
                return Response(serializer.data, status=status.HTTP_200_OK)
            else:
                return Response({'error': 'Email does not match registration number.'}, status=status.HTTP_401_UNAUTHORIZED)
        except User.DoesNotExist:
            user = User.objects.create(
                registration_number=reg_number,
                email=email,
                full_name=name
            )
            serializer = UserSerializer(user)
            return Response(serializer.data, status=status.HTTP_200_OK)

class UserProfileView(APIView):
    def get(self, request, user_id):
        user = get_object_or_404(User, id=user_id)
        return Response(UserSerializer(user).data)

    def patch(self, request, user_id):
        user = get_object_or_404(User, id=user_id)
        serializer = UserSerializer(user, data=request.data, partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class ItemListView(APIView):
    def get(self, request):
        # Return ALL items, ordering available ones first, then by creation date
        items = Item.objects.select_related('owner', 'category').prefetch_related('reviews', 'reviews__reviewer').order_by('-is_available', '-created_at')
        serializer = ItemSerializer(items, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)

    def post(self, request):
        serializer = ItemSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class ItemDetailView(APIView):
    def delete(self, request, item_id):
        item = get_object_or_404(Item, id=item_id)
        # In a real app, check if request.user == item.owner
        item.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)

    def get(self, request, item_id):
        item = get_object_or_404(Item.objects.select_related('owner', 'category').prefetch_related('reviews', 'reviews__reviewer'), id=item_id)
        serializer = ItemSerializer(item)
        return Response(serializer.data)

class BookingRequestView(APIView):
    def post(self, request):
        serializer = BookingSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class UserBookingsView(APIView):
    def get(self, request, user_id):
        from django.db.models import Case, When, IntegerField
        
        status_order = Case(
            When(status='APPROVED', then=1),
            When(status='PENDING', then=2),
            When(status='REJECTED', then=3),
            When(status='COMPLETED', then=4),
            default=5,
            output_field=IntegerField(),
        )

        # Optimized with select_related and prefetch_related to get info in ONE query
        borrowed = Booking.objects.filter(borrower_id=user_id).select_related('item', 'item__owner', 'borrower').prefetch_related('messages').order_by(status_order, '-request_date')
        received = Booking.objects.filter(item__owner_id=user_id).select_related('item', 'item__owner', 'borrower').prefetch_related('messages').order_by(status_order, '-request_date')
        
        context = {'request': request}
        return Response({
            'sent': BookingSerializer(borrowed, many=True, context=context).data,
            'received': BookingSerializer(received, many=True, context=context).data
        }, status=status.HTTP_200_OK)

class BookingUpdateView(APIView):
    def patch(self, request, booking_id):
        booking = get_object_or_404(Booking, id=booking_id)
        new_status = request.data.get('status')
        if new_status in ['APPROVED', 'REJECTED', 'COMPLETED']:
            booking.status = new_status
            booking.save()
            
            # Logic to update item availability
            item = booking.item
            if new_status == 'APPROVED':
                item.is_available = False
                item.save()
                from django.utils import timezone
                import datetime
                booking.due_date = timezone.now() + datetime.timedelta(days=item.max_borrow_days)
                booking.save()
            elif new_status == 'COMPLETED':
                item.is_available = True
                item.save()
                
                from django.utils import timezone
                if booking.due_date and timezone.now() > booking.due_date:
                    days_late = (timezone.now() - booking.due_date).days
                    booking.penalty_amount = max(1, days_late) * 50
                    booking.save()
                
                # Update stats and Award Points
                owner = item.owner
                borrower = booking.borrower
                transfer_amount = 10
                
                if owner:
                    owner.items_lent += 1
                    owner.points += transfer_amount
                    self.update_level(owner)
                    
                    owner.trust_score += (transfer_amount * 0.2) * owner.level
                    owner.save()
                    
                if borrower:
                    borrower.items_borrowed += 1
                    if borrower.points >= transfer_amount:
                        borrower.points -= transfer_amount
                    else:
                        borrower.points = 0
                    
                    self.update_level(borrower)
                    
                    # Deduct penalty from borrower's trust score
                    if booking.penalty_amount > 0:
                        borrower.trust_score -= (booking.penalty_amount / 10.0) # Penalty impacts trust
                    else:
                        # Reward for returning on time!
                        borrower.trust_score += 5.0
                    borrower.save()
            elif new_status == 'REJECTED' and booking.status == 'APPROVED':
                # If it was approved but then rejected (edge case), make it available again
                item.is_available = True
                item.save()
                
            return Response(BookingSerializer(booking).data, status=status.HTTP_200_OK)
        return Response({'error': 'Invalid status'}, status=status.HTTP_400_BAD_REQUEST)

    def update_level(self, user):
        if user.points >= 5000:
            user.level = 5
        elif user.points >= 1500:
            user.level = 4
        elif user.points >= 500:
            user.level = 3
        elif user.points >= 100:
            user.level = 2
        else:
            user.level = 1

    def get(self, request, booking_id):
        booking = get_object_or_404(Booking, id=booking_id)
        return Response(BookingSerializer(booking).data)

class CategoryListView(APIView):
    def get(self, request):
        categories = Category.objects.all()
        return Response(CategorySerializer(categories, many=True).data)

class MessageListView(APIView):
    def get(self, request, booking_id):
        booking = get_object_or_404(Booking, id=booking_id)
        if booking.status != 'APPROVED' and booking.status != 'COMPLETED':
            return Response({'error': 'Chat not available'}, status=status.HTTP_403_FORBIDDEN)

        user_id = request.query_params.get('user_id')
        if user_id:
            Message.objects.filter(booking_id=booking_id, receiver_id=user_id, status='SENT').update(status='SEEN')

        limit = int(request.query_params.get('limit', 50))
        messages = Message.objects.filter(booking_id=booking_id).select_related('sender', 'receiver').order_by('timestamp')
        return Response(MessageSerializer(messages, many=True).data)

    def post(self, request, booking_id):
        booking = get_object_or_404(Booking, id=booking_id)
        sender_id = str(request.data.get('sender', '')).strip().lower()
        message_text = request.data.get('message_text') or request.data.get('content')
        
        borrower_id = str(booking.borrower.id).strip().lower()
        owner_id = str(booking.item.owner.id).strip().lower()
        
        receiver = booking.item.owner if sender_id == borrower_id else booking.borrower

        data = {
            'booking': booking_id,
            'sender': sender_id,
            'receiver': receiver.id,
            'message_text': message_text,
            'status': 'SENT'
        }
        
        serializer = MessageSerializer(data=data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class TypingIndicatorView(APIView):
    def post(self, request, booking_id):
        # In a real real-time app, this would be a WebSocket event.
        # Here we just return a "User is typing" state if another user recently updated it.
        # For simplicity, we'll just echo back or store it in a cache.
        is_typing = request.data.get('is_typing', False)
        user_id = request.data.get('user_id')
        # ... logic to store typing state (e.g. in Redis or Cache)
        return Response({'status': 'ok'})

class ReviewCreateView(APIView):
    def post(self, request):
        serializer = ReviewSerializer(data=request.data)
        if serializer.is_valid():
            review = serializer.save()
            
            # Update item's cached rating and reviews count
            review.item.update_rating()
            
            # Update item owner's trust score and award points
            item = review.item
            owner = item.owner
            if owner:
                # Average all reviews for items owned by this user
                all_reviews = Review.objects.filter(item__owner=owner)
                if all_reviews.exists():
                    from django.db.models import Avg
                    avg_rating = all_reviews.aggregate(Avg('rating'))['rating__avg']
                    # No longer overriding the trust score with just 1-5 rating
                
                # Bonus points for 5-star review
                if review.rating == 5:
                    owner.points += 20
                    owner.trust_score += 5.0
                    # Check for level up
                    self.update_level(owner)
                
                owner.save()
            
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def update_level(self, user):
        if user.points >= 5000:
            user.level = 5
        elif user.points >= 1500:
            user.level = 4
        elif user.points >= 500:
            user.level = 3
        elif user.points >= 100:
            user.level = 2
        else:
            user.level = 1

class UserReviewsView(APIView):
    def get(self, request, user_id):
        # Get reviews for all items owned by this user
        reviews = Review.objects.filter(item__owner_id=user_id).select_related('reviewer', 'item').order_by('-created_at')
        return Response(ReviewSerializer(reviews, many=True).data)

class LeaderboardListView(APIView):
    def get(self, request):
        # Rank by items_lent (primary) and trust_score (secondary)
        users = User.objects.all().order_by('-items_lent', '-trust_score')[:10]
        return Response(UserSerializer(users, many=True).data)
