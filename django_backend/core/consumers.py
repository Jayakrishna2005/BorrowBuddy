import json
from channels.generic.websocket import AsyncWebsocketConsumer
from channels.db import database_sync_to_async
from .models import Message, Booking, User
from django.utils import timezone

class ChatConsumer(AsyncWebsocketConsumer):
    async def connect(self):
        self.request_id = self.scope['url_route']['kwargs']['request_id']
        self.room_group_name = f'chat_{self.request_id}'

        # Join room group
        await self.channel_layer.group_add(
            self.room_group_name,
            self.channel_name
        )

        await self.accept()

    async def disconnect(self, close_code):
        # Leave room group
        await self.channel_layer.group_discard(
            self.room_group_name,
            self.channel_name
        )

    # Receive message from WebSocket
    async def receive(self, text_data):
        data = json.loads(text_data)
        message_type = data.get('type', 'chat_message')

        if message_type == 'mark_seen':
            await self.mark_messages_as_seen(self.request_id)
            await self.channel_layer.group_send(
                self.room_group_name,
                {
                    'type': 'messages_seen',
                    'request_id': self.request_id
                }
            )
            return

        message_text = data.get('message')
        sender_id = data.get('sender_id')
        receiver_id = data.get('receiver_id')

        if not message_text:
            return

        # Save message to database
        saved_message = await self.save_message(self.request_id, sender_id, receiver_id, message_text)

        # Send message to room group
        await self.channel_layer.group_send(
            self.room_group_name,
            {
                'type': 'chat_message',
                'message': message_text,
                'sender_id': str(sender_id),
                'receiver_id': str(receiver_id) if receiver_id else None,
                'timestamp': saved_message.timestamp.isoformat(),
                'message_id': str(saved_message.id),
                'status': saved_message.status,
                'is_seen': saved_message.is_seen
            }
        )

    # Receive message from room group
    async def chat_message(self, event):
        # Send message to WebSocket
        await self.send(text_data=json.dumps({
            'type': 'chat_message',
            'message': event['message'],
            'sender_id': event['sender_id'],
            'receiver_id': event['receiver_id'],
            'timestamp': event['timestamp'],
            'message_id': event['message_id'],
            'status': event['status'],
            'is_seen': event.get('is_seen', False)
        }))

    async def messages_seen(self, event):
        await self.send(text_data=json.dumps({
            'type': 'messages_seen',
            'request_id': event['request_id']
        }))

    @database_sync_to_async
    def mark_messages_as_seen(self, request_id):
        Message.objects.filter(booking_id=request_id, is_seen=False).update(is_seen=True, status='SEEN')

    @database_sync_to_async
    def save_message(self, request_id, sender_id, receiver_id, message_text):
        try:
            return Message.objects.create(
                booking_id=request_id,
                sender_id=sender_id,
                receiver_id=receiver_id,
                message_text=message_text,
                status='SENT'
            )
        except Exception as e:
            print(f"Error saving message: {e}")
            return None
