import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const API_BASE_URL = 'http://localhost:8000/api/v1';

function Requests({ user, refreshUser }) {
  const [requests, setRequests] = useState({ sent: [], received: [] });
  const [activeTab, setActiveTab] = useState('received');
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const [reviewModalBooking, setReviewModalBooking] = useState(null);
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');



  useEffect(() => {
    if (user) fetchRequests();
  }, [user]);

  const fetchRequests = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/users/${user.id}/bookings/?user_id=${user.id}`);
      const data = await response.json();
      
      const sortBookings = (bookings) => {
        if (!bookings) return [];
        const statusOrder = {
          'APPROVED': 1,
          'PENDING': 2,
          'REJECTED': 3,
          'COMPLETED': 4
        };
        return [...bookings].sort((a, b) => {
          const statusDiff = (statusOrder[a.status] || 99) - (statusOrder[b.status] || 99);
          if (statusDiff !== 0) return statusDiff;
          return new Date(b.request_date) - new Date(a.request_date);
        });
      };

      setRequests({
        sent: sortBookings(data.sent),
        received: sortBookings(data.received)
      });
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateStatus = async (bookingId, newStatus) => {
    try {
      const response = await fetch(`${API_BASE_URL}/bookings/${bookingId}/`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: newStatus })
      });
      if (response.ok) {
        fetchRequests();
        if (refreshUser) refreshUser();
        if (newStatus === 'COMPLETED') {
          navigate('/thank-you');
        }
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleSubmitReview = async () => {
    if (!comment.trim()) {
      alert("Please write a comment for the review.");
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/reviews/`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          booking: reviewModalBooking.id,
          item: reviewModalBooking.item,
          reviewer: user.id,
          rating: rating,
          comment: comment.trim()
        })
      });

      if (response.ok) {
        alert("Review submitted successfully! Trust score updated.");
        setReviewModalBooking(null);
        fetchRequests();
        if (refreshUser) refreshUser();
      } else {
        const errorData = await response.json();
        alert(`Failed to submit review: ${errorData.error || 'Unknown error'}`);
      }
    } catch (err) {
      console.error(err);
      alert("Network error. Failed to submit review.");
    }
  };



  const renderBookingCard = (booking, type) => {
    const isOwner = type === 'received';
    const otherUser = isOwner ? booking.borrower_name : booking.owner_name;
    
    return (
      <div key={booking.id} className="glass-card" style={{ padding: '1.5rem', marginBottom: '1rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h4 style={{ fontSize: '1.2rem' }}>{booking.item_name}</h4>
          <span style={{ 
            padding: '4px 10px', 
            borderRadius: '20px', 
            fontSize: '0.8rem', 
            fontWeight: 'bold',
            background: booking.status === 'APPROVED' ? 'rgba(16, 185, 129, 0.2)' : 
                        booking.status === 'REJECTED' ? 'rgba(239, 68, 68, 0.2)' :
                        booking.status === 'COMPLETED' ? 'rgba(59, 130, 246, 0.2)' : 'rgba(255, 255, 255, 0.1)',
            color: booking.status === 'APPROVED' ? 'var(--success)' : 
                   booking.status === 'REJECTED' ? 'var(--danger)' :
                   booking.status === 'COMPLETED' ? '#3b82f6' : 'var(--text-main)'
          }}>
            {booking.status}
          </span>
        </div>
        
        <div style={{ display: 'flex', gap: '1rem' }}>
          {booking.item_image && (
            <img src={booking.item_image.startsWith('http') ? booking.item_image : `http://localhost:8000${booking.item_image}`} alt="item" style={{ width: '80px', height: '80px', objectFit: 'cover', borderRadius: '8px' }} />
          )}
          <div>
            <p style={{ color: 'var(--text-muted)' }}>{isOwner ? 'Requested by' : 'Owned by'}: <strong style={{ color: 'var(--text-main)' }}>{otherUser}</strong></p>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', margin: '4px 0' }}>Requested on: {new Date(booking.request_date).toLocaleDateString()}</p>

          </div>
        </div>

        <div style={{ display: 'flex', gap: '1rem', marginTop: 'auto' }}>
          {/* Owner Actions */}
          {isOwner && booking.status === 'PENDING' && (
            <>
              <button className="btn btn-primary" onClick={() => handleUpdateStatus(booking.id, 'APPROVED')} style={{ background: 'var(--success)' }}>Approve</button>
              <button className="btn btn-primary" onClick={() => handleUpdateStatus(booking.id, 'REJECTED')} style={{ background: 'var(--danger)' }}>Reject</button>
            </>
          )}
          {isOwner && booking.status === 'APPROVED' && (
            <button className="btn btn-primary" onClick={() => handleUpdateStatus(booking.id, 'COMPLETED')} style={{ background: '#3b82f6' }}>Mark Returned</button>
          )}

          {/* Both Users Actions */}
          {(booking.status === 'APPROVED' || booking.status === 'COMPLETED') && (
            <button className="btn" style={{ background: 'var(--surface-color)', border: '1px solid var(--primary)', color: 'var(--primary)', position: 'relative' }} onClick={() => navigate(`/chat/${booking.id}`)}>
              Open Chat
              {booking.unread_count > 0 && (
                <span style={{ position: 'absolute', top: '-8px', right: '-8px', background: 'var(--danger)', color: 'white', borderRadius: '50%', width: '22px', height: '22px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.75rem', fontWeight: 'bold', boxShadow: '0 2px 4px rgba(0,0,0,0.2)' }}>
                  {booking.unread_count}
                </span>
              )}
            </button>
          )}

          {/* Borrower Actions */}
          {!isOwner && booking.status === 'COMPLETED' && !booking.has_review && (
            <button 
              className="btn btn-primary" 
              onClick={() => {
                setRating(5);
                setComment('');
                setReviewModalBooking(booking);
              }}
              style={{ background: 'var(--success)' }}
            >
              Review Item
            </button>
          )}
          {!isOwner && booking.status === 'COMPLETED' && booking.has_review && (
            <span style={{ color: 'var(--success)', fontWeight: 'bold', fontSize: '0.9rem', display: 'flex', alignItems: 'center' }}>
              ✓ Reviewed
            </span>
          )}

        </div>
      </div>
    );
  };

  return (
    <div className="fade-in">
      <h2 style={{ fontSize: '2rem', marginBottom: '1.5rem' }}>Your Requests</h2>
      
      <div style={{ display: 'flex', gap: '1rem', marginBottom: '2rem' }}>
        <button 
          className={`btn ${activeTab === 'received' ? 'btn-primary' : ''}`}
          style={activeTab !== 'received' ? { background: 'var(--glass-bg)', color: 'var(--text-main)', position: 'relative' } : { position: 'relative' }}
          onClick={() => setActiveTab('received')}
        >
          Received Requests
          {requests.received?.some(r => r.unread_count > 0) && (
            <span style={{ marginLeft: '8px', background: 'var(--danger)', color: 'white', borderRadius: '12px', padding: '2px 8px', fontSize: '0.7rem', fontWeight: 'bold' }}>New</span>
          )}
        </button>
        <button 
          className={`btn ${activeTab === 'sent' ? 'btn-primary' : ''}`}
          style={activeTab !== 'sent' ? { background: 'var(--glass-bg)', color: 'var(--text-main)', position: 'relative' } : { position: 'relative' }}
          onClick={() => setActiveTab('sent')}
        >
          Sent Requests
          {requests.sent?.some(r => r.unread_count > 0) && (
            <span style={{ marginLeft: '8px', background: 'var(--danger)', color: 'white', borderRadius: '12px', padding: '2px 8px', fontSize: '0.7rem', fontWeight: 'bold' }}>New</span>
          )}
        </button>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '4rem' }}>
          <div className="loader" style={{ width: '40px', height: '40px' }}></div>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(400px, 1fr))', gap: '1.5rem' }}>
          {requests[activeTab].map(booking => renderBookingCard(booking, activeTab))}
          {requests[activeTab].length === 0 && (
            <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
              No requests found.
            </div>
          )}
        </div>
      )}      {reviewModalBooking && (
        <div className="fade-in" style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.8)', zIndex: 9999, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '1rem' }} onClick={() => setReviewModalBooking(null)}>
          <div style={{ background: 'var(--glass-bg)', backdropFilter: 'blur(16px)', padding: '2rem', borderRadius: '24px', width: '100%', maxWidth: '450px', border: '1px solid var(--glass-border)' }} onClick={e => e.stopPropagation()}>
            <h3 style={{ marginBottom: '0.5rem', color: 'var(--text-main)', fontSize: '1.4rem', fontWeight: 'bold', textAlign: 'center' }}>Review Item</h3>
            <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem', textAlign: 'center', fontSize: '0.95rem' }}>
              How was your experience borrowing <strong style={{ color: 'var(--text-main)' }}>{reviewModalBooking.item_name}</strong>?
            </p>
            
            <div style={{ display: 'flex', justifyContent: 'center', gap: '0.5rem', marginBottom: '1.5rem' }}>
              {[1, 2, 3, 4, 5].map(star => (
                <span 
                  key={star} 
                  onClick={() => setRating(star)} 
                  style={{ 
                    fontSize: '2.5rem', 
                    cursor: 'pointer', 
                    color: star <= rating ? '#FFB400' : 'rgba(0,0,0,0.1)',
                    textShadow: star <= rating ? '0 0 10px rgba(255, 180, 0, 0.4)' : 'none',
                    transition: 'all 0.2s ease'
                  }}
                >
                  ★
                </span>
              ))}
            </div>

            <textarea 
              placeholder="Write a comment about the item's condition, utility, or owner..." 
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              rows={4}
              required
              style={{ marginBottom: '1.5rem', resize: 'none' }}
            />

            <div style={{ display: 'flex', gap: '1rem' }}>
              <button 
                onClick={() => setReviewModalBooking(null)} 
                className="btn" 
                style={{ flex: 1, background: 'rgba(0,0,0,0.05)', color: 'var(--text-main)', border: '1px solid rgba(0,0,0,0.1)' }}
              >
                Cancel
              </button>
              <button 
                onClick={handleSubmitReview} 
                className="btn btn-primary" 
                style={{ flex: 1 }}
              >
                Submit Review
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}

export default Requests;
