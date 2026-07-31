from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.shortcuts import get_object_or_404
from .models import User, Item, Booking, Category, Message, Review
from .serializers import UserSerializer, ItemSerializer, BookingSerializer, CategorySerializer, MessageSerializer, ReviewSerializer
from django.core.mail import send_mail
from django.conf import settings

import random
import threading
from django.contrib.auth.hashers import make_password, check_password

class AuthRegisterView(APIView):
    def post(self, request):
        name = request.data.get('name')
        reg_number = request.data.get('regNumber')
        email = request.data.get('email', '').strip()
        password = request.data.get('password')

        if not all([name, reg_number, email, password]):
            return Response({'error': 'All fields are required.'}, status=status.HTTP_400_BAD_REQUEST)

        # If registering with a new/different password or updating registration,
        # delete any existing unverified or verified user with the same email/registration number
        # so they can register as a fresh user and get a new OTP, except for ujaya78901@gmail.com.
        if email.strip().lower() != 'ujaya78901@gmail.com':
            User.objects.filter(email__iexact=email).delete()
            User.objects.filter(registration_number=reg_number).delete()

        # Check if email or reg_number already exists
        user_by_email = User.objects.filter(email__iexact=email).first()
        user_by_reg = User.objects.filter(registration_number=reg_number).first()

        user = None

        if user_by_reg:
            if user_by_reg.is_email_verified:
                return Response({'error': 'Registration number already verified with an account.'}, status=status.HTTP_400_BAD_REQUEST)
            else:
                user = user_by_reg
                user.email = email
                user.full_name = name

        elif user_by_email:
             if user_by_email.is_email_verified:
                  return Response({'error': 'Email already verified with an account.'}, status=status.HTTP_400_BAD_REQUEST)
             else:
                  user = user_by_email
                  user.registration_number = reg_number
                  user.full_name = name

        if not user:
             user = User.objects.create(
                registration_number=reg_number,
                email=email,
                full_name=name
            )
        
        user.password = make_password(password)
        # Generate 6 digit OTP
        otp = str(random.randint(100000, 999999))
        user.otp = otp
        user.is_email_verified = False
        user.save()
        
        # Send email asynchronously
        def send_otp_email():
            try:
                send_mail(
                    subject='Welcome to BorrowBuddy - Verify Your Email',
                    message=f'Hello {name},\n\nYour OTP for registration is: {otp}\n\nWelcome to the community!',
                    from_email=settings.DEFAULT_FROM_EMAIL,
                    recipient_list=[email],
                    fail_silently=False,
                )
            except Exception as e:
                print(f"Failed to send email: {e}")
                print(f"--- OTP for {email}: {otp} ---")
                
        threading.Thread(target=send_otp_email).start()
        
        return Response({'message': 'OTP sent to email.'}, status=status.HTTP_200_OK)

class AuthVerifyOTPView(APIView):
    def post(self, request):
        email = request.data.get('email', '').strip()
        otp = request.data.get('otp')

        try:
            user = User.objects.get(email=email)
            if user.otp == otp:
                user.is_email_verified = True
                user.otp = None
                user.save()
                serializer = UserSerializer(user)
                return Response(serializer.data, status=status.HTTP_200_OK)
            else:
                return Response({'error': 'Invalid OTP.'}, status=status.HTTP_400_BAD_REQUEST)
        except User.DoesNotExist:
            return Response({'error': 'User not found.'}, status=status.HTTP_404_NOT_FOUND)

class AuthLoginView(APIView):
    def post(self, request):
        email = request.data.get('email', '').strip()
        password = request.data.get('password')

        if not email or not password:
            # Fallback for old login method that used regNumber and email only
            reg_number = request.data.get('regNumber')
            if reg_number and email:
                 try:
                     user = User.objects.get(registration_number=reg_number, email__iexact=email)
                     if not user.is_email_verified:
                         # Automatically verify for old users
                         user.is_email_verified = True
                         user.save()
                     serializer = UserSerializer(user)
                     return Response(serializer.data, status=status.HTTP_200_OK)
                 except User.DoesNotExist:
                     return Response({'error': 'Invalid credentials.'}, status=status.HTTP_401_UNAUTHORIZED)
            return Response({'error': 'Email and password are required.'}, status=status.HTTP_400_BAD_REQUEST)

        try:
            user = User.objects.get(email__iexact=email)
            if not user.is_email_verified:
                return Response({'error': 'Email not verified.', 'needs_verification': True}, status=status.HTTP_401_UNAUTHORIZED)
            
            if check_password(password, user.password):
                serializer = UserSerializer(user)
                return Response(serializer.data, status=status.HTTP_200_OK)
            else:
                return Response({'error': 'Invalid email or password.'}, status=status.HTTP_401_UNAUTHORIZED)
        except User.DoesNotExist:
            return Response({'error': 'Invalid email or password.'}, status=status.HTTP_401_UNAUTHORIZED)

class AuthForgotPasswordView(APIView):
    def post(self, request):
        email = request.data.get('email', '').strip()
        try:
            user = User.objects.get(email__iexact=email)
            otp = str(random.randint(100000, 999999))
            user.otp = otp
            user.save()
            def send_pwd_email():
                try:
                    send_mail(
                        subject='BorrowBuddy - Password Reset OTP',
                        message=f'Hello {user.full_name},\n\nYour OTP for password reset is: {otp}\n\nIf you did not request this, please ignore this email.',
                        from_email=settings.DEFAULT_FROM_EMAIL,
                        recipient_list=[email],
                        fail_silently=False,
                    )
                except Exception as e:
                    print(f"Failed to send email: {e}")
                    print(f"--- Password Reset OTP for {email}: {otp} ---")
            
            threading.Thread(target=send_pwd_email).start()
                
            return Response({'message': 'OTP sent to email.'}, status=status.HTTP_200_OK)
        except User.DoesNotExist:
            return Response({'error': 'User with this email does not exist.'}, status=status.HTTP_404_NOT_FOUND)

class AuthResetPasswordView(APIView):
    def post(self, request):
        email = request.data.get('email', '').strip()
        otp = request.data.get('otp')
        new_password = request.data.get('newPassword')

        try:
            user = User.objects.get(email__iexact=email)
            if user.otp == otp:
                user.password = make_password(new_password)
                user.otp = None
                user.is_email_verified = True
                user.save()
                return Response({'message': 'Password reset successful.'}, status=status.HTTP_200_OK)
            else:
                return Response({'error': 'Invalid OTP.'}, status=status.HTTP_400_BAD_REQUEST)
        except User.DoesNotExist:
            return Response({'error': 'User not found.'}, status=status.HTTP_404_NOT_FOUND)

class AuthChangePasswordView(APIView):
    def post(self, request, user_id):
        old_password = request.data.get('oldPassword')
        new_password = request.data.get('newPassword')
        
        user = get_object_or_404(User, id=user_id)
        
        if check_password(old_password, user.password):
            user.password = make_password(new_password)
            user.save()
            return Response({'message': 'Password changed successfully.'}, status=status.HTTP_200_OK)
        else:
            return Response({'error': 'Incorrect old password.'}, status=status.HTTP_400_BAD_REQUEST)

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
        from django.db.models import Case, When, IntegerField, Count, Q
        
        status_order = Case(
            When(status='APPROVED', then=1),
            When(status='PENDING', then=2),
            When(status='REJECTED', then=3),
            When(status='COMPLETED', then=4),
            default=5,
            output_field=IntegerField(),
        )

        unread_filter = Q(messages__receiver_id=user_id, messages__status='SENT')

        borrowed = Booking.objects.filter(borrower_id=user_id)\
            .select_related('item', 'item__owner', 'borrower')\
            .annotate(annotated_unread_count=Count('messages', filter=unread_filter))\
            .order_by(status_order, '-request_date')
            
        received = Booking.objects.filter(item__owner_id=user_id)\
            .select_related('item', 'item__owner', 'borrower')\
            .annotate(annotated_unread_count=Count('messages', filter=unread_filter))\
            .order_by(status_order, '-request_date')
        
        context = {'request': request}
        return Response({
            'sent': BookingSerializer(borrowed, many=True, context=context).data,
            'received': BookingSerializer(received, many=True, context=context).data
        }, status=status.HTTP_200_OK)


class BookingUpdateView(APIView):
    def patch(self, request, booking_id):
        booking = get_object_or_404(Booking, id=booking_id)
        old_status = booking.status
        new_status = request.data.get('status')
        if new_status in ['APPROVED', 'REJECTED', 'COMPLETED']:
            item = booking.item
            if new_status == 'APPROVED' and old_status != 'APPROVED':
                # Check quantity
                if item.quantity < booking.quantity:
                    return Response({'error': f'Insufficient quantity available. Only {item.quantity} remaining.'}, status=status.HTTP_400_BAD_REQUEST)
                
                # Perform approval updates
                item.quantity = max(0, item.quantity - booking.quantity)
                if item.quantity == 0:
                    item.is_available = False
                item.save()
                
                from django.utils import timezone
                import datetime
                booking.due_date = timezone.now() + datetime.timedelta(days=item.max_borrow_days)
                
                # Share points and update stats/trust scores upon approval
                borrower = booking.borrower
                owner = item.owner
                if owner and borrower:
                    owner.items_lent += 1
                    borrower.items_borrowed += 1
                    borrower.points = max(0, borrower.points - 5)
                    owner.points += 5
                    owner.trust_score += 5.0
                    borrower.trust_score += 5.0
                    owner.update_level_based_on_points()
                    borrower.update_level_based_on_points()
                    owner.save()
                    borrower.save()

            elif new_status == 'COMPLETED' and old_status == 'APPROVED':
                from django.utils import timezone
                booking.return_date = timezone.now()
                
                # Make the item available again and add back the quantity
                item.quantity += booking.quantity
                item.is_available = True
                item.save()
                
            elif new_status == 'REJECTED':
                if old_status == 'APPROVED':
                    item.quantity += booking.quantity
                    item.is_available = True
                    item.save()
                    
                    # Revert points and stats if previously approved
                    borrower = booking.borrower
                    owner = item.owner
                    if owner and borrower:
                        owner.items_lent = max(0, owner.items_lent - 1)
                        borrower.items_borrowed = max(0, borrower.items_borrowed - 1)
                        owner.points = max(0, owner.points - 5)
                        borrower.points += 5
                        owner.trust_score = max(0.0, owner.trust_score - 5.0)
                        borrower.trust_score = max(0.0, borrower.trust_score - 5.0)
                        owner.update_level_based_on_points()
                        borrower.update_level_based_on_points()
                        owner.save()
                        borrower.save()

            booking.status = new_status
            booking.save()
            return Response(BookingSerializer(booking).data, status=status.HTTP_200_OK)
        return Response({'error': 'Invalid status update.'}, status=status.HTTP_400_BAD_REQUEST)


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
                # Bonus points and trust score for 5-star review
                if review.rating == 5:
                    owner.points += 20
                    owner.trust_score += 5.0
                
                # Additional 1 point to the trust score for good reviews (rating >= 4)
                if review.rating >= 4:
                    owner.trust_score += 1.0
                
                # Check for level up
                owner.update_level_based_on_points()
                owner.save()
            
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

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
