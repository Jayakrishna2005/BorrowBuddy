import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const API_BASE_URL = 'http://localhost:8000/api/v1';

function Settings({ user, setUser }) {
  const [fullName, setFullName] = useState(user.fullName || '');
  const [image, setImage] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();

  const handleImageChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setImage(e.target.files[0]);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    const data = new FormData();
    data.append('full_name', fullName);
    if (image) {
      data.append('profile_photo', image);
    }

    try {
      const response = await fetch(`${API_BASE_URL}/users/${user.id}/`, {
        method: 'PATCH',
        body: data
      });

      if (response.ok) {
        const updatedUser = await response.json();
        setUser(updatedUser);
        localStorage.setItem('user', JSON.stringify(updatedUser));
        setSuccess('Profile updated successfully!');
        setTimeout(() => navigate('/profile'), 1500);
      } else {
        setError('Failed to update profile.');
      }
    } catch (err) {
      console.error(err);
      setError('Network error.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fade-in" style={{ maxWidth: '500px', margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '2rem' }}>
        <button className="btn" style={{ padding: '0.5rem 1rem' }} onClick={() => navigate('/profile')}>← Back</button>
        <h2 style={{ fontSize: '2rem' }}>Settings</h2>
      </div>

      <div className="glass-card" style={{ padding: '2rem' }}>
        {error && <div style={{ color: 'var(--danger)', marginBottom: '1rem' }}>{error}</div>}
        {success && <div style={{ color: 'var(--success)', marginBottom: '1rem' }}>{success}</div>}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          
          <div style={{ textAlign: 'center' }}>
            <div style={{ 
              width: '120px', height: '120px', borderRadius: '50%', 
              background: 'var(--bg-color)', color: 'var(--text-muted)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              border: '2px dashed var(--primary)', margin: '0 auto 1rem', overflow: 'hidden',
              position: 'relative'
            }}>
              {image ? (
                <img src={URL.createObjectURL(image)} alt="Preview" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
              ) : (user.profile_photo ? (
                <img src={user.profile_photo.startsWith('http') ? user.profile_photo : `http://localhost:8000${user.profile_photo}`} alt="Profile" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
              ) : (
                <span>No Photo</span>
              ))}
            </div>
            <label style={{ cursor: 'pointer', color: 'var(--primary)', fontWeight: 'bold' }}>
              Change Profile Photo
              <input type="file" accept="image/*" onChange={handleImageChange} style={{ display: 'none' }} />
            </label>
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Full Name</label>
            <input 
              type="text" 
              value={fullName} 
              onChange={e => setFullName(e.target.value)} 
              required 
            />
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>Registration Number</label>
            <input 
              type="text" 
              value={user.registrationNumber} 
              disabled 
              style={{ background: '#E5E7EB', color: '#9CA3AF' }}
            />
          </div>

          <button type="submit" className="btn btn-primary" disabled={loading} style={{ marginTop: '1rem', padding: '1rem' }}>
            {loading ? <span className="loader"></span> : 'Save Changes'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default Settings;
