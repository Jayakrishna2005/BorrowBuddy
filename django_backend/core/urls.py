from django.urls import path
from .views import AuthLoginView, ItemListView, BookingRequestView

urlpatterns = [
    path('api/v1/auth/login', AuthLoginView.as_view(), name='login'),
    path('api/v1/items', ItemListView.as_view(), name='items'),
    path('api/v1/requests', BookingRequestView.as_view(), name='booking_request'),
]
