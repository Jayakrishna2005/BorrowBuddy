from django.db import connection
try:
    connection.ensure_connection()
    print('Connected')
except Exception as e:
    print(f'Failed: {e}')
