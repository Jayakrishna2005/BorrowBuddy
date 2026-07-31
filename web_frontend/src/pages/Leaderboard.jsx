import React, { useState, useEffect } from 'react';

const API_BASE_URL = 'http://localhost:8000/api/v1';

function Leaderboard({ user, setUser }) {
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
      
      const current = data.find(u => u.id === user?.id);
      if (current && setUser) {
        setUser(current);
        localStorage.setItem('user', JSON.stringify(current));
      }
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
              <div style={{ textAlign: 'center' }}>Sentiment Score</div>
            </div>
            
            {users.map((u, index) => {
              const isCurrentUser = u.id === user?.id;
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
                  <div style={{ textAlign: 'center', color: 'var(--success)', fontWeight: 'bold' }}>
                    {u.sellerSentiment !== undefined ? `${u.sellerSentiment}%` : '100%'}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      <div style={{ marginTop: '3rem' }}>
        <h3 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <span>Your Badges & Achievements</span> 🏅
        </h3>
        
        {/* Badges Display Grid */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1.5rem' }}>
          {(() => {
            const userLevel = user?.level || 1;
            const badgesList = [
              { level: 1, name: "Novice", points: 0, icon: "🌱", color: "#a8a29e", desc: "Default starting badge. Requires 0+ pts, 0 items shared, and 0 items borrowed." },
              { level: 2, name: "Helper", points: 100, icon: "🤝", color: "#10b981", desc: "Requires Lvl 2: 100+ pts, 1+ item shared, and 1+ item borrowed." },
              { level: 3, name: "Rising Star", points: 500, icon: "⭐", color: "#3b82f6", desc: "Requires Lvl 3: 500+ pts, 3+ items shared, and 3+ items borrowed." },
              { level: 4, name: "Community Hero", points: 1500, icon: "🛡️", color: "#8b5cf6", desc: "Requires Lvl 4: 1500+ pts, 8+ items shared, and 8+ items borrowed." },
              { level: 5, name: "Legend", points: 5000, icon: "👑", color: "#f59e0b", desc: "Requires Lvl 5: 5000+ pts, 15+ items shared, and 15+ items borrowed." }
            ];

            return badgesList.map((b) => {
              const isUnlocked = userLevel >= b.level;
              const isActive = userLevel === b.level;

              return (
                <div 
                  key={b.name} 
                  className="glass-card" 
                  style={{ 
                    padding: '1.5rem', 
                    textAlign: 'center', 
                    position: 'relative',
                    opacity: isUnlocked ? 1 : 0.5,
                    border: isActive ? `2px solid ${b.color}` : '1px solid var(--glass-border)',
                    boxShadow: isActive ? `0 0 15px ${b.color}40` : 'none',
                    transition: 'all 0.3s ease',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'space-between',
                    minHeight: '220px'
                  }}
                >
                  <div>
                    {/* Badge Icon */}
                    <div style={{ 
                      fontSize: '3rem', 
                      marginBottom: '1rem',
                      filter: isUnlocked ? 'none' : 'grayscale(100%)',
                      transform: isActive ? 'scale(1.1)' : 'none',
                      transition: 'transform 0.3s ease'
                    }}>
                      {b.icon}
                    </div>

                    {/* Badge Title */}
                    <h4 style={{ margin: '0 0 0.5rem 0', color: isUnlocked ? 'var(--text-main)' : 'var(--text-muted)', fontSize: '1.2rem' }}>
                      {b.name}
                    </h4>

                    {/* Required Points */}
                    <span style={{ 
                      fontSize: '0.8rem', 
                      background: isUnlocked ? `${b.color}20` : 'rgba(255,255,255,0.05)', 
                      color: isUnlocked ? b.color : 'var(--text-muted)', 
                      padding: '2px 8px', 
                      borderRadius: '12px',
                      fontWeight: 'bold' 
                    }}>
                      Lvl {b.level} ({b.points}+ pts)
                    </span>

                    {/* Description */}
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginTop: '1rem', marginBottom: 0 }}>
                      {b.desc}
                    </p>
                  </div>

                  {/* Status Indicator */}
                  <div style={{ marginTop: '1.5rem' }}>
                    {isActive ? (
                      <span style={{ 
                        background: `linear-gradient(135deg, ${b.color}, #d97706)`, 
                        color: 'white', 
                        padding: '4px 12px', 
                        borderRadius: '20px', 
                        fontSize: '0.75rem', 
                        fontWeight: 'bold',
                        boxShadow: '0 2px 8px rgba(0,0,0,0.2)'
                      }}>
                        Active Badge 🏆
                      </span>
                    ) : isUnlocked ? (
                      <span style={{ color: 'var(--success)', fontSize: '0.8rem', fontWeight: 'bold' }}>
                        Unlocked 🔓
                      </span>
                    ) : (
                      <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>
                        Locked 🔒
                      </span>
                    )}
                  </div>
                </div>
              );
            });
          })()}
        </div>
      </div>

    </div>
  );
}

export default Leaderboard;
