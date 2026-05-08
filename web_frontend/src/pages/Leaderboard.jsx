import React, { useState, useEffect } from 'react';

const API_BASE_URL = 'http://localhost:8000/api/v1';

function Leaderboard({ user }) {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchLeaderboard();
  }, []);

  const fetchLeaderboard = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/leaderboard/`);
      const data = await response.json();
      setUsers(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fade-in" style={{ maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
        <h2 style={{ fontSize: '2.5rem', background: 'linear-gradient(to right, #ffd700, #ff8c00)', WebkitBackgroundClip: 'text', color: 'transparent' }}>
          Leaderboard 🏆
        </h2>
        <p style={{ color: 'var(--text-muted)' }}>Top helpers in the BorrowBuddy community</p>
      </div>

      <div className="glass-card" style={{ padding: '2rem' }}>
        {loading ? (
          <div style={{ textAlign: 'center', padding: '2rem' }}>
            <div className="loader" style={{ width: '40px', height: '40px' }}></div>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div style={{ display: 'grid', gridTemplateColumns: '50px 1fr 100px 100px 100px', gap: '1rem', padding: '1rem', borderBottom: '1px solid var(--glass-border)', color: 'var(--text-muted)', fontWeight: 'bold' }}>
              <div>Rank</div>
              <div>User</div>
              <div style={{ textAlign: 'center' }}>Level</div>
              <div style={{ textAlign: 'center' }}>Points</div>
              <div style={{ textAlign: 'center' }}>Trust</div>
            </div>
            
            {users.map((u, index) => {
              const isCurrentUser = u.id === user.id;
              return (
                <div 
                  key={u.id} 
                  style={{ 
                    display: 'grid', 
                    gridTemplateColumns: '50px 1fr 100px 100px 100px', 
                    gap: '1rem', 
                    padding: '1rem', 
                    borderRadius: '12px',
                    background: isCurrentUser ? 'rgba(108, 92, 231, 0.1)' : 'transparent',
                    border: isCurrentUser ? '1px solid var(--primary)' : '1px solid transparent',
                    alignItems: 'center'
                  }}
                >
                  <div style={{ 
                    fontSize: '1.2rem', 
                    fontWeight: 'bold', 
                    color: index === 0 ? '#ffd700' : index === 1 ? '#c0c0c0' : index === 2 ? '#cd7f32' : 'var(--text-muted)' 
                  }}>
                    #{index + 1}
                  </div>
                  <div style={{ fontWeight: 'bold' }}>
                    {u.fullName} {isCurrentUser && <span style={{ fontSize: '0.8rem', color: 'var(--primary)', marginLeft: '8px' }}>(You)</span>}
                  </div>
                  <div style={{ textAlign: 'center', color: 'var(--primary)', fontWeight: 'bold' }}>Lvl {u.level}</div>
                  <div style={{ textAlign: 'center', color: 'var(--secondary)' }}>{u.points}</div>
                  <div style={{ textAlign: 'center', color: 'var(--success)' }}>{u.trustScore}</div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      <div style={{ marginTop: '2rem' }}>
        <h3 style={{ marginBottom: '1rem' }}>Badges & Rules 🏅</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
          <div className="glass-card" style={{ padding: '1rem', textAlign: 'center' }}>
            <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>🤝</div>
            <h4>Help a Borrower</h4>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>+50 Points</p>
          </div>
          <div className="glass-card" style={{ padding: '1rem', textAlign: 'center' }}>
            <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>⭐</div>
            <h4>5-Star Review</h4>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>+20 Points & Trust</p>
          </div>
          <div className="glass-card" style={{ padding: '1rem', textAlign: 'center' }}>
            <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>📈</div>
            <h4>Level Up</h4>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>100, 500, 1500, 5000 pts</p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Leaderboard;
