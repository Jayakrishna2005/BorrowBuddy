from django.urls import path
from .views import AuthLoginView, ItemListView

urlpatterns = [
    path('api/v1/auth/login', AuthLoginView.as_view(), name='login'),
    path('api/v1/items', ItemListView.as_view(), name='items'),
]
