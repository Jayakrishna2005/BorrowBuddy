import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

const API_BASE_URL = 'http://localhost:8000/api/v1';

function Chat({ user }) {
  const { bookingId } = useParams();
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [bookingDetails, setBookingDetails] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toastMessage, setToastMessage] = useState(null);
  const navigate = useNavigate();
  const messagesEndRef = useRef(null);

  useEffect(() => {
    fetchBookingDetails();
    fetchMessages();
    
    // Connect to WebSocket instead of heavy polling
    const ws = new WebSocket(`ws://localhost:8000/ws/chat/${bookingId}/`);
    
    ws.onmessage = (event) => {
      const data = JSON.parse(event.data);
      if (data.type === 'chat_message') {
        const newMsg = {
          id: data.message_id,
          booking: bookingId,
          sender: data.sender_id,
          message_text: data.message,
          timestamp: data.timestamp,
          status: data.status,
          is_seen: data.is_seen
        };
        
        setMessages(prev => {
          if (!prev.find(m => m.id === newMsg.id)) {
             if (String(newMsg.sender) !== String(user.id)) {
                setToastMessage('New message received! 💬');
                try {
                   const audio = new Audio('https://www.soundjay.com/buttons/sounds/button-09.mp3');
                   audio.play();
                } catch(e) {}
                setTimeout(() => setToastMessage(null), 3000);
             }
             return [...prev, newMsg];
          }
          return prev;
        });
      }
    };
    
    return () => ws.close();
  }, [bookingId]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const fetchBookingDetails = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/bookings/${bookingId}/`);
      if (response.ok) {
        const data = await response.json();
        setBookingDetails(data);
      } else {
        setError('Could not load booking details');
      }
    } catch (err) {
      console.error(err);
    }
  };

  const fetchMessages = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/bookings/${bookingId}/messages/?user_id=${user.id}`);
      if (response.ok) {
        const data = await response.json();
        setMessages(data);
      } else if (response.status === 403) {
        setError('Chat not available. Booking must be APPROVED or COMPLETED.');
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!newMessage.trim()) return;

    const messageText = newMessage;
    setNewMessage('');
    
    // Optimistic UI update
    setMessages(prev => [...prev, {
      id: Date.now(),
      sender: user.id,
      message_text: messageText,
      timestamp: new Date().toISOString()
    }]);

    try {
      const response = await fetch(`${API_BASE_URL}/bookings/${bookingId}/messages/`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          sender: user.id,
          message_text: messageText
        })
      });
      
      if (response.ok) {
        fetchMessages();
      }
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) return <div className="loader" style={{ margin: '4rem auto', display: 'block', width: '40px', height: '40px' }}></div>;
  if (error) return <div style={{ color: 'var(--danger)', padding: '2rem', textAlign: 'center' }}>{error}</div>;

  const isOwner = bookingDetails?.item_owner_id === user.id;
  const otherUserName = isOwner ? bookingDetails?.borrower_name : bookingDetails?.owner_name;

  return (
    <div className="fade-in" style={{ height: 'calc(100vh - 150px)', display: 'flex', flexDirection: 'column', maxWidth: '800px', margin: '0 auto', position: 'relative' }}>
      {toastMessage && (
        <div className="fade-in" style={{ position: 'absolute', top: '20px', left: '50%', transform: 'translateX(-50%)', background: 'var(--primary)', color: 'white', padding: '0.75rem 1.5rem', borderRadius: '30px', fontWeight: 'bold', zIndex: 1000, boxShadow: '0 4px 12px rgba(0,0,0,0.3)' }}>
          {toastMessage}
        </div>
      )}
      <div className="glass-card" style={{ padding: '1.5rem', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <button className="btn" style={{ padding: '0.5rem 1rem' }} onClick={() => navigate('/requests')}>← Back</button>
        <div>
          <h2 style={{ fontSize: '1.2rem' }}>Chat with {otherUserName}</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>Regarding: {bookingDetails?.item_name}</p>
        </div>
      </div>

      <div className="glass-card" style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <div style={{ flex: 1, overflowY: 'auto', padding: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {messages.length === 0 ? (
            <p style={{ textAlign: 'center', color: 'var(--text-muted)', marginTop: '2rem' }}>No messages yet. Say hello!</p>
          ) : (
            messages.map(msg => {
              const isMine = String(msg.sender) === String(user.id);
              return (
                <div key={msg.id} style={{ alignSelf: isMine ? 'flex-end' : 'flex-start', maxWidth: '70%' }}>
                  <div style={{
                    padding: '1rem',
                    borderRadius: '16px',
                    borderBottomRightRadius: isMine ? '4px' : '16px',
                    borderBottomLeftRadius: !isMine ? '4px' : '16px',
                    background: isMine ? 'linear-gradient(135deg, var(--primary), #8e2de2)' : 'var(--glass-bg)',
                    color: isMine ? 'white' : 'var(--text-main)',
                    boxShadow: 'var(--card-shadow)'
                  }}>
                    {msg.message_text}
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '4px', textAlign: isMine ? 'right' : 'left' }}>
                    {new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </div>
                </div>
              );
            })
          )}
          <div ref={messagesEndRef} />
        </div>

        <form onSubmit={handleSendMessage} style={{ padding: '1rem', borderTop: '1px solid var(--glass-border)', display: 'flex', gap: '1rem', background: 'rgba(0,0,0,0.2)' }}>
          <input 
            type="text" 
            placeholder="Type your message..." 
            value={newMessage}
            onChange={e => setNewMessage(e.target.value)}
            style={{ flex: 1, background: 'var(--surface-color)' }}
          />
          <button type="submit" className="btn btn-primary" disabled={!newMessage.trim()}>Send</button>
        </form>
      </div>
    </div>
  );
}

export default Chat;
