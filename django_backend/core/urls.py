from django.urls import path
from .views import AuthLoginView, ItemListView, BookingRequestView, UserBookingsView, BookingUpdateView, CategoryListView, MessageListView, ReviewCreateView, ItemDetailView, UserProfileView, LeaderboardListView, UserReviewsView

urlpatterns = [
    path('api/v1/auth/login/', AuthLoginView.as_view(), name='login'),
    path('api/v1/auth/profile/<uuid:user_id>/', UserProfileView.as_view(), name='profile'),
    path('api/v1/leaderboard/', LeaderboardListView.as_view(), name='leaderboard'),
    path('api/v1/items/', ItemListView.as_view(), name='items'),
    path('api/v1/items/<uuid:item_id>/', ItemDetailView.as_view(), name='item_detail'),
    path('api/v1/requests/', BookingRequestView.as_view(), name='booking_request'),
    path('api/v1/users/<uuid:user_id>/bookings/', UserBookingsView.as_view(), name='user_bookings'),
    path('api/v1/bookings/<uuid:booking_id>/', BookingUpdateView.as_view(), name='booking_update'),
    path('api/v1/categories/', CategoryListView.as_view(), name='categories'),
    path('api/v1/bookings/<uuid:booking_id>/messages/', MessageListView.as_view(), name='messages'),
    path('api/v1/reviews/', ReviewCreateView.as_view(), name='reviews'),
    path('api/v1/users/<uuid:user_id>/reviews/', UserReviewsView.as_view(), name='user_reviews'),
]
