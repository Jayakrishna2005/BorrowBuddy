import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';

const API_BASE_URL = 'http://localhost:8000/api/v1';

function Profile({ user, setUser }) {
  const [profileData, setProfileData] = useState(user);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    if (user?.id) {
      fetchProfile();
    }
  }, [user?.id]);

  const fetchProfile = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/profile/${user.id}/`);
      if (response.ok) {
        const data = await response.json();
        setProfileData(data);
        if (setUser) setUser(data);
        localStorage.setItem('user', JSON.stringify(data));
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

  const profilePhotoUrl = profileData?.profile_photo 
    ? (profileData.profile_photo.startsWith('http') ? profileData.profile_photo : `http://localhost:8000${profileData.profile_photo}`) 
    : null;

  return (
    <div className="fade-in" style={{ maxWidth: '600px', margin: '0 auto' }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', marginBottom: '2rem' }}>
        <div style={{ 
          width: '80px', height: '80px', borderRadius: '50%', 
          background: 'var(--primary)', color: 'white',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: '2rem', fontWeight: 'bold', overflow: 'hidden'
        }}>
          {profilePhotoUrl ? (
            <img src={profilePhotoUrl} alt="Profile" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
          ) : (
            profileData?.fullName?.charAt(0) || 'U'
          )}
        </div>
        <div style={{ marginLeft: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
            <h2 style={{ fontSize: '1.5rem', margin: 0 }}>{profileData?.fullName}</h2>
            <span style={{ background: 'var(--primary)', color: 'white', padding: '2px 8px', borderRadius: '8px', fontSize: '0.75rem', fontWeight: 'bold' }}>
              Lvl {profileData?.level || 1}
            </span>
            {profileData?.badge && (
              <span style={{ background: 'linear-gradient(135deg, #f59e0b, #ea580c)', color: 'white', padding: '2px 8px', borderRadius: '8px', fontSize: '0.75rem', fontWeight: 'bold' }}>
                {profileData.badge}
              </span>
            )}
          </div>
          <p style={{ color: 'var(--text-muted)' }}>{profileData?.email}</p>
        </div>
      </div>

      {/* Stats */}
      <div style={{ display: 'flex', gap: '1rem', marginBottom: '2.5rem' }}>
        <div className="glass-card" style={{ flex: 1, padding: '1.5rem', textAlign: 'center' }}>
          <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'var(--primary)' }}>{profileData?.itemsLent || 0}</div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Shared</div>
        </div>
        <div className="glass-card" style={{ flex: 1, padding: '1.5rem', textAlign: 'center' }}>
          <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'var(--primary)' }}>{profileData?.itemsBorrowed || 0}</div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Borrowed</div>
        </div>
        <div className="glass-card" style={{ flex: 1, padding: '1.5rem', textAlign: 'center' }}>
          <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'var(--primary)' }}>{profileData?.trustScore || 0}★</div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Trust</div>
        </div>
        <div className="glass-card" style={{ flex: 1, padding: '1.5rem', textAlign: 'center' }}>
          <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#f59e0b' }}>{profileData?.points || 0}</div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Points</div>
        </div>
      </div>

      {/* Badge Progress Section */}
      {(() => {
        const currentPoints = profileData?.points || 0;
        const currentLevel = profileData?.level || 1;
        const currentShared = profileData?.itemsLent || 0;
        const currentBorrowed = profileData?.itemsBorrowed || 0;

        let pointsTarget = 100;
        let sharedTarget = 1;
        let borrowedTarget = 1;
        let nextBadgeName = "Helper";

        if (currentLevel === 2) {
          pointsTarget = 500;
          sharedTarget = 3;
          borrowedTarget = 3;
          nextBadgeName = "Rising Star";
        } else if (currentLevel === 3) {
          pointsTarget = 1500;
          sharedTarget = 8;
          borrowedTarget = 8;
          nextBadgeName = "Community Hero";
        } else if (currentLevel === 4) {
          pointsTarget = 5000;
          sharedTarget = 15;
          borrowedTarget = 15;
          nextBadgeName = "Legend";
        } else if (currentLevel >= 5) {
          pointsTarget = 5000;
          sharedTarget = 15;
          borrowedTarget = 15;
          nextBadgeName = "Max Level";
        }

        const currentPointsDisplay = Math.max(0, currentPoints - 50);
        const pointsTargetDisplay = pointsTarget - 50;

        const pointsPct = currentLevel >= 5 ? 100 : Math.min(100, Math.round((currentPointsDisplay / pointsTargetDisplay) * 100));
        const sharedPct = currentLevel >= 5 ? 100 : Math.min(100, Math.round((currentShared / sharedTarget) * 100));
        const borrowedPct = currentLevel >= 5 ? 100 : Math.min(100, Math.round((currentBorrowed / borrowedTarget) * 100));

        return (
          <div className="glass-card fade-in" style={{ padding: '2rem', marginBottom: '2.5rem', borderLeft: '4px solid #f59e0b' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <div>
                <span style={{ fontSize: '0.95rem', color: 'var(--text-muted)' }}>Active Badge:</span>
                <strong style={{ marginLeft: '0.5rem', color: '#f59e0b', fontSize: '1.2rem' }}>
                  {profileData?.badge || "Novice"} 🏅
                </strong>
              </div>
              {currentLevel < 5 ? (
                <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                  Requirements for <strong>{nextBadgeName}</strong> (Level {currentLevel + 1})
                </span>
              ) : (
                <span style={{ fontSize: '0.85rem', color: 'var(--success)', fontWeight: 'bold' }}>
                  🎉 Legendary Status Reached
                </span>
              )}
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.2rem' }}>
              {/* Points Progress */}
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '0.4rem' }}>
                  <span style={{ fontWeight: 'bold' }}>Points / Score</span>
                  <span style={{ color: 'var(--text-muted)' }}>{currentPointsDisplay} / {pointsTargetDisplay} pts ({pointsPct}%)</span>
                </div>
                <div style={{ background: 'rgba(255, 255, 255, 0.1)', borderRadius: '10px', height: '10px', width: '100%', overflow: 'hidden' }}>
                  <div style={{ background: 'linear-gradient(90deg, var(--primary) 0%, #f59e0b 100%)', height: '100%', width: `${pointsPct}%`, borderRadius: '10px', transition: 'width 0.5s ease-in-out' }}></div>
                </div>
              </div>

              {/* Items Shared Progress */}
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '0.4rem' }}>
                  <span style={{ fontWeight: 'bold' }}>Items Shared (Lent)</span>
                  <span style={{ color: 'var(--text-muted)' }}>{currentShared} / {sharedTarget} items ({sharedPct}%)</span>
                </div>
                <div style={{ background: 'rgba(255, 255, 255, 0.1)', borderRadius: '10px', height: '10px', width: '100%', overflow: 'hidden' }}>
                  <div style={{ background: 'linear-gradient(90deg, #10b981 0%, #059669 100%)', height: '100%', width: `${sharedPct}%`, borderRadius: '10px', transition: 'width 0.5s ease-in-out' }}></div>
                </div>
              </div>

              {/* Items Borrowed Progress */}
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '0.4rem' }}>
                  <span style={{ fontWeight: 'bold' }}>Items Borrowed</span>
                  <span style={{ color: 'var(--text-muted)' }}>{currentBorrowed} / {borrowedTarget} items ({borrowedPct}%)</span>
                </div>
                <div style={{ background: 'rgba(255, 255, 255, 0.1)', borderRadius: '10px', height: '10px', width: '100%', overflow: 'hidden' }}>
                  <div style={{ background: 'linear-gradient(90deg, #3b82f6 0%, #2563eb 100%)', height: '100%', width: `${borrowedPct}%`, borderRadius: '10px', transition: 'width 0.5s ease-in-out' }}></div>
                </div>
              </div>
            </div>

            {currentLevel < 5 && (
              <div style={{ marginTop: '1.5rem', textAlign: 'center', fontSize: '0.85rem', color: 'var(--text-muted)', borderTop: '1px solid var(--glass-border)', paddingTop: '1rem' }}>
                💡 Complete borrowings and lendings to earn points, level up, and unlock your next badge!
              </div>
            )}
          </div>
        );
      })()}

      {/* Menu Options */}
      <h3 style={{ fontSize: '1.2rem', marginBottom: '1rem' }}>Menu</h3>
      <div className="glass-card" style={{ display: 'flex', flexDirection: 'column' }}>
        <Link to="/" style={{ padding: '1rem 1.5rem', borderBottom: '1px solid var(--glass-border)', color: 'var(--text-main)', display: 'flex', justifyContent: 'space-between' }}>
          <span>My Listings</span>
          <span style={{ color: 'var(--text-muted)' }}>→</span>
        </Link>
        <Link to="/requests" style={{ padding: '1rem 1.5rem', borderBottom: '1px solid var(--glass-border)', color: 'var(--text-main)', display: 'flex', justifyContent: 'space-between' }}>
          <span>My Requests</span>
          <span style={{ color: 'var(--text-muted)' }}>→</span>
        </Link>

        <Link to="/leaderboard" style={{ padding: '1rem 1.5rem', borderBottom: '1px solid var(--glass-border)', color: 'var(--text-main)', display: 'flex', justifyContent: 'space-between' }}>
          <span>Badges & Leaderboard</span>
          <span style={{ color: 'var(--text-muted)' }}>→</span>
        </Link>
        <Link to="/settings" style={{ padding: '1rem 1.5rem', color: 'var(--text-main)', display: 'flex', justifyContent: 'space-between' }}>
          <span>Settings</span>
          <span style={{ color: 'var(--text-muted)' }}>→</span>
        </Link>
      </div>
    </div>
  );
}

export default Profile;
