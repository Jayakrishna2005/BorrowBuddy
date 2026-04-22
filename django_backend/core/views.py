from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.shortcuts import get_object_or_404
from .models import User, Item
from .serializers import UserSerializer, ItemSerializer

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


class ItemListView(APIView):
    def get(self, request):
        items = Item.objects.filter(is_available=True)
        serializer = ItemSerializer(items, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)

    def post(self, request):
        serializer = ItemSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
