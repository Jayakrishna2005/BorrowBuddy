import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const API_BASE_URL = 'http://localhost:8000/api/v1';

function Requests({ user }) {
  const [requests, setRequests] = useState({ sent: [], received: [] });
  const [activeTab, setActiveTab] = useState('received');
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  // Review modal state
  const [showReviewModal, setShowReviewModal] = useState(false);
  const [selectedBookingForReview, setSelectedBookingForReview] = useState(null);
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
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleSubmitReview = async () => {
    if (!selectedBookingForReview) return;
    try {
      const response = await fetch(`${API_BASE_URL}/reviews/`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          booking: selectedBookingForReview.id,
          item: selectedBookingForReview.item,
          reviewer: user.id,
          rating,
          comment
        })
      });
      if (response.ok) {
        setShowReviewModal(false);
        fetchRequests();
        alert('Review submitted successfully!');
      }
    } catch (err) {
      console.error(err);
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
            {booking.due_date && (
              <p style={{ color: new Date() > new Date(booking.due_date) && booking.status !== 'COMPLETED' ? 'var(--danger)' : 'var(--text-main)', fontSize: '0.9rem', margin: '4px 0', fontWeight: 'bold' }}>
                Due on: {new Date(booking.due_date).toLocaleDateString()}
              </p>
            )}
            {booking.penalty_amount > 0 && (
              <p style={{ color: 'var(--danger)', fontSize: '0.9rem', margin: '4px 0', fontWeight: 'bold' }}>
                Penalty: {booking.penalty_amount} units
              </p>
            )}
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
            <button className="btn btn-primary" onClick={() => {
              handleUpdateStatus(booking.id, 'COMPLETED');
              alert('Item has been returned and is now available on the homepage for others to borrow!');
            }} style={{ background: '#3b82f6' }}>Mark Returned</button>
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

          {/* Borrower Review Action */}
          {!isOwner && booking.status === 'COMPLETED' && !booking.has_review && (
            <button className="btn btn-primary" onClick={() => {
              setSelectedBookingForReview(booking);
              setShowReviewModal(true);
            }}>
              Leave Review
            </button>
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
      )}

      {/* Review Modal */}
      {showReviewModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.8)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 }}>
          <div className="glass-card fade-in" style={{ padding: '2rem', width: '90%', maxWidth: '500px' }}>
            <h3 style={{ marginBottom: '1rem' }}>Review {selectedBookingForReview?.item_name}</h3>
            
            <div style={{ marginBottom: '1rem' }}>
              <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Rating (1-5)</label>
              <input type="number" min="1" max="5" value={rating} onChange={e => setRating(Number(e.target.value))} />
            </div>
            
            <div style={{ marginBottom: '1.5rem' }}>
              <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Comment</label>
              <textarea 
                value={comment} 
                onChange={e => setComment(e.target.value)}
                style={{ width: '100%', padding: '1rem', borderRadius: '12px', border: '1px solid var(--glass-border)', minHeight: '100px' }}
              />
            </div>
            
            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
              <button className="btn" style={{ background: 'transparent', color: 'var(--text-main)' }} onClick={() => setShowReviewModal(false)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleSubmitReview}>Submit Review</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Requests;
