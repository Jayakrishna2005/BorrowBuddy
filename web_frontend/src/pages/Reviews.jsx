import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const API_BASE_URL = 'http://localhost:8000/api/v1';

function Reviews({ user }) {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchReviews();
  }, [user.id]);

  const fetchReviews = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/users/${user.id}/reviews/`);
      if (response.ok) {
        const data = await response.json();
        setReviews(data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loader" style={{ display: 'block', margin: '4rem auto' }}></div>;
  }

  return (
    <div className="fade-in" style={{ maxWidth: '600px', margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '2rem' }}>
        <button className="btn" style={{ padding: '0.5rem 1rem' }} onClick={() => navigate('/profile')}>← Back</button>
        <h2 style={{ fontSize: '2rem' }}>My Reviews</h2>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        {reviews.length === 0 ? (
          <div className="glass-card" style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            You haven't received any reviews yet. Keep lending items to earn trust!
          </div>
        ) : (
          reviews.map(review => (
            <div key={review.id} className="glass-card" style={{ padding: '1.5rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                <strong style={{ fontSize: '1.1rem' }}>{review.item_title}</strong>
                <div style={{ background: 'rgba(76, 175, 80, 0.1)', color: 'var(--success)', padding: '2px 8px', borderRadius: '8px', fontWeight: 'bold' }}>
                  {review.rating} ⭐
                </div>
              </div>
              <p style={{ color: 'var(--text-main)', marginBottom: '1rem' }}>"{review.comment}"</p>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                <span>By: {review.reviewer_name}</span>
                <span>{new Date(review.created_at).toLocaleDateString()}</span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default Reviews;
