from rest_framework import serializers
from .models import User, Item, Category, Booking

class UserSerializer(serializers.ModelSerializer):
    fullName = serializers.CharField(source='full_name', required=False)
    registrationNumber = serializers.CharField(source='registration_number', required=False)
    trustScore = serializers.IntegerField(source='trust_score', read_only=True)
    itemsLent = serializers.IntegerField(source='items_lent', read_only=True)
    itemsBorrowed = serializers.IntegerField(source='items_borrowed', read_only=True)

    class Meta:
        model = User
        fields = ['id', 'email', 'fullName', 'registrationNumber', 'trustScore', 'itemsLent', 'itemsBorrowed']

class ItemSerializer(serializers.ModelSerializer):
    isAvailable = serializers.BooleanField(source='is_available', required=False)
    
    class Meta:
        model = Item
        fields = ['id', 'title', 'description', 'condition', 'isAvailable']

class BookingSerializer(serializers.ModelSerializer):
    class Meta:
        model = Booking
        fields = '__all__'
