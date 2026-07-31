from rest_framework import serializers
from .models import User, Item, Category, Booking, Message, Review

class UserSerializer(serializers.ModelSerializer):
    fullName = serializers.CharField(source='full_name', required=False)
    registrationNumber = serializers.CharField(source='registration_number', required=False)
    trustScore = serializers.IntegerField(source='trust_score', read_only=True)
    itemsLent = serializers.IntegerField(source='items_lent', read_only=True)
    itemsBorrowed = serializers.IntegerField(source='items_borrowed', read_only=True)
    points = serializers.IntegerField(read_only=True)
    level = serializers.IntegerField(read_only=True)
    badge = serializers.SerializerMethodField()
    sellerSentiment = serializers.SerializerMethodField()

    class Meta:
        model = User
        fields = ['id', 'email', 'fullName', 'registrationNumber', 'trustScore', 'itemsLent', 'itemsBorrowed', 'points', 'level', 'badge', 'profile_photo', 'sellerSentiment']

    def get_badge(self, obj):
        level = obj.level
        if level >= 5: return "Legend"
        if level == 4: return "Community Hero"
        if level == 3: return "Rising Star"
        if level == 2: return "Helper"
        return "Novice"

    def get_sellerSentiment(self, obj):
        return obj.get_seller_sentiment_percentage()

class CategorySerializer(serializers.ModelSerializer):
    class Meta:
        model = Category
        fields = ['id', 'name']

class ItemSerializer(serializers.ModelSerializer):
    category_name = serializers.SerializerMethodField()
    owner_id = serializers.SerializerMethodField()
    owner_name = serializers.SerializerMethodField()
    owner_trust_score = serializers.SerializerMethodField()
    owner_level = serializers.SerializerMethodField()
    owner_badge = serializers.SerializerMethodField()
    owner_sentiment = serializers.SerializerMethodField()
    reviews = serializers.SerializerMethodField()

    def get_category_name(self, obj):
        return obj.category.name if obj.category else None

    def get_owner_id(self, obj):
        return obj.owner.id if obj.owner else None
    
    def get_owner_name(self, obj):
        return obj.owner.full_name if obj.owner else "Unknown"

    def get_owner_trust_score(self, obj):
        return obj.owner.trust_score if obj.owner else 0.0

    def get_owner_level(self, obj):
        return obj.owner.level if obj.owner else 1

    def get_owner_badge(self, obj):
        if not obj.owner:
            return "Novice"
        level = obj.owner.level
        if level >= 5: return "Legend"
        if level == 4: return "Community Hero"
        if level == 3: return "Rising Star"
        if level == 2: return "Helper"
        return "Novice"
    
    def get_owner_sentiment(self, obj):
        return obj.owner.get_seller_sentiment_percentage() if obj.owner else 100

    def get_reviews(self, obj):
        reviews = obj.reviews.all().order_by('-created_at')
        return ReviewSerializer(reviews, many=True).data

    class Meta:
        model = Item
        fields = [
            'id', 'title', 'description', 'condition', 'is_available', 
            'max_borrow_days', 'quantity', 'category_name', 'owner_id', 'owner_name', 
            'owner_trust_score', 'owner_level', 'owner_badge', 'owner_sentiment',
            'image', 'category', 'owner', 'average_rating', 'reviews_count', 'reviews'
        ]

        extra_kwargs = {
            'category': {'write_only': True},
            'owner': {'write_only': True},
            'description': {'required': False},
            'condition': {'required': False},
            'max_borrow_days': {'required': False}
        }

class BookingSerializer(serializers.ModelSerializer):
    item_name = serializers.CharField(source='item.title', read_only=True)
    item_image = serializers.SerializerMethodField()
    owner_name = serializers.CharField(source='item.owner.full_name', read_only=True)
    borrower_name = serializers.CharField(source='borrower.full_name', read_only=True)
    has_review = serializers.SerializerMethodField()
    
    unread_count = serializers.SerializerMethodField()
    
    item_owner_id = serializers.CharField(source='item.owner.id', read_only=True)
    penalty_amount = serializers.SerializerMethodField()
    
    class Meta:
        model = Booking
        fields = ['id', 'item', 'borrower', 'status', 'request_date', 'due_date', 'return_date', 'penalty_amount', 'quantity', 'item_name', 'item_image', 'owner_name', 'item_owner_id', 'borrower_name', 'has_review', 'unread_count']

    def validate(self, attrs):
        item = attrs.get('item')
        quantity = attrs.get('quantity', 1)
        if item:
            if not item.is_available or item.quantity <= 0:
                raise serializers.ValidationError({"error": "Item is currently not available."})
            if quantity > item.quantity:
                raise serializers.ValidationError({"error": f"Requested quantity ({quantity}) exceeds available quantity ({item.quantity})."})
            if quantity < 1:
                raise serializers.ValidationError({"error": "Quantity must be at least 1."})
        return attrs

    def get_penalty_amount(self, obj):
        from django.utils import timezone
        if obj.status == 'APPROVED' and obj.due_date and timezone.now() > obj.due_date:
            days_late = (timezone.now() - obj.due_date).days
            return max(1, days_late) * 50 # 50 units penalty per day
        elif obj.status == 'COMPLETED':
            return obj.penalty_amount
        return 0

    def get_unread_count(self, obj):
        if hasattr(obj, 'annotated_unread_count'):
            return obj.annotated_unread_count
            
        request = self.context.get('request')
        if not request:
            return 0
        user_id = request.query_params.get('user_id') # We'll pass user_id to the getUserBookings call
        if not user_id:
            return 0
        
        # Check if messages are prefetched to avoid N+1 queries
        if hasattr(obj, '_prefetched_objects_cache') and 'messages' in obj._prefetched_objects_cache:
            return sum(1 for m in obj.messages.all() if str(m.receiver_id) == str(user_id) and m.status == 'SENT')
            
        return obj.messages.filter(receiver_id=user_id, status='SENT').count()

    def get_item_image(self, obj):
        if obj.item and obj.item.image:
            return obj.item.image.url
        return None

    def get_has_review(self, obj):
        return hasattr(obj, 'review')

class MessageSerializer(serializers.ModelSerializer):
    class Meta:
        model = Message
        fields = '__all__'

class ReviewSerializer(serializers.ModelSerializer):
    reviewer_name = serializers.CharField(source='reviewer.full_name', read_only=True)
    item_title = serializers.CharField(source='item.title', read_only=True)

    class Meta:
        model = Review
        fields = ['id', 'booking', 'item', 'item_title', 'reviewer', 'reviewer_name', 'rating', 'comment', 'created_at']
