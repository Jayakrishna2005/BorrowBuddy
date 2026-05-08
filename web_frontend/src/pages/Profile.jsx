import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';

const API_BASE_URL = 'http://localhost:8000/api/v1';

function Profile({ user }) {
  const [profileData, setProfileData] = useState(user);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchProfile();
  }, [user.id]);

  const fetchProfile = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/profile/${user.id}/`);
      if (response.ok) {
        const data = await response.json();
        setProfileData(data);
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
        <Link to="/reviews" style={{ padding: '1rem 1.5rem', borderBottom: '1px solid var(--glass-border)', color: 'var(--text-main)', display: 'flex', justifyContent: 'space-between' }}>
          <span>Reviews</span>
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
