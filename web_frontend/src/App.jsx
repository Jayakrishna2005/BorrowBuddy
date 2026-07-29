import React, { useState, useEffect } from 'react';
import { Routes, Route, useNavigate, useLocation, Link } from 'react-router-dom';
import Login from './pages/Login';
import Home from './pages/Home';
import Requests from './pages/Requests';
import Chat from './pages/Chat';
import PostItem from './pages/PostItem';
import Leaderboard from './pages/Leaderboard';
import Profile from './pages/Profile';
import Settings from './pages/Settings';
import Splash from './pages/Splash';
import Onboarding from './pages/Onboarding';
import ThankYou from './pages/ThankYou';

function App() {
  const [user, setUser] = useState(null);
  const [totalUnread, setTotalUnread] = useState(null);
  const [globalToast, setGlobalToast] = useState(null);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      setUser(JSON.parse(storedUser));
    }
  }, []);

  useEffect(() => {
    if (!user?.id) return;
    const fetchUnread = async () => {
      try {
        const res = await fetch(`http://localhost:8000/api/v1/users/${user.id}/bookings/?user_id=${user.id}`);
        if (res.ok) {
          const data = await res.json();
          let unread = 0;
          data.sent?.forEach(b => unread += b.unread_count);
          data.received?.forEach(b => unread += b.unread_count);
          
          setTotalUnread(prev => {
            if (prev !== null && unread > prev) {
              if (!window.location.pathname.startsWith('/chat/')) {
                 setGlobalToast('New message received! Click to view.');
                 try {
                   const audio = new Audio('https://www.soundjay.com/buttons/sounds/button-09.mp3');
                   audio.play();
                 } catch(e) {}
                 setTimeout(() => setGlobalToast(null), 5000);
              }
            }
            return unread;
          });
        }
      } catch (err) {}
    };

    fetchUnread();
    const interval = setInterval(fetchUnread, 30000); // Polling every 30 seconds instead of 3 seconds
    return () => clearInterval(interval);
  }, [user?.id]);

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (!storedUser) {
      if (!['/login', '/splash', '/onboarding'].includes(location.pathname)) {
        navigate('/splash');
      }
    } else {
      if (['/login', '/splash', '/onboarding'].includes(location.pathname)) {
        navigate('/');
      }
    }
  }, [navigate, location.pathname]);

  const handleLogout = () => {
    localStorage.removeItem('user');
    setUser(null);
    navigate('/login');
  };

  const refreshUser = async () => {
    if (!user?.id) return;
    try {
      const res = await fetch(`http://localhost:8000/api/v1/auth/profile/${user.id}/`);
      if (res.ok) {
        const data = await res.json();
        setUser(data);
        localStorage.setItem('user', JSON.stringify(data));
      }
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <>
      {globalToast && (
        <div className="fade-in" style={{ position: 'fixed', top: '20px', right: '20px', background: 'var(--primary)', color: 'white', padding: '1rem 1.5rem', borderRadius: '12px', fontWeight: 'bold', zIndex: 9999, boxShadow: '0 4px 12px rgba(0,0,0,0.3)', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '10px' }} onClick={() => { setGlobalToast(null); navigate('/requests'); }}>
          <span style={{ fontSize: '1.2rem' }}>💬</span>
          {globalToast}
        </div>
      )}
      {user && (
        <nav className="navbar">
          <Link to="/" className="nav-brand" style={{ display: 'flex', alignItems: 'center', gap: '8px', textDecoration: 'none' }}>
            <img src="/logo.png" alt="BorrowBuddy Logo" style={{ width: '38px', height: '38px', objectFit: 'contain', borderRadius: '8px' }} />
            <span style={{ 
              fontWeight: '900', 
              fontSize: '1.4rem',
              color: 'var(--primary)',
              letterSpacing: '-1px'
            }}>BorrowBuddy</span>
          </Link>
          <div className="nav-links">
            <Link to="/" className={`nav-link ${location.pathname === '/' ? 'active' : ''}`}>Home</Link>
            <Link to="/requests" className={`nav-link ${location.pathname === '/requests' ? 'active' : ''}`} style={{ position: 'relative' }}>
              Requests
              {totalUnread > 0 && (
                <span style={{ position: 'absolute', top: '-5px', right: '-15px', background: 'var(--danger)', color: 'white', borderRadius: '50%', width: '20px', height: '20px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.7rem', fontWeight: 'bold' }}>
                  {totalUnread}
                </span>
              )}
            </Link>
            <Link to="/post" className={`nav-link ${location.pathname === '/post' ? 'active' : ''}`}>Post Item</Link>
            <Link to="/leaderboard" className={`nav-link ${location.pathname === '/leaderboard' ? 'active' : ''}`}>Leaderboard</Link>
            <Link to="/profile" className={`nav-link ${location.pathname.startsWith('/profile') || location.pathname === '/settings' ? 'active' : ''}`}>Profile</Link>
            <div className="nav-link" onClick={handleLogout} style={{ color: 'var(--danger)' }}>Logout</div>
          </div>
        </nav>
      )}
      
      <main className="app-container" style={{ padding: '2rem 1rem' }}>
        <Routes>
          <Route path="/splash" element={<Splash onFinish={() => navigate('/onboarding')} />} />
          <Route path="/onboarding" element={<Onboarding onFinish={() => navigate('/login')} />} />
          <Route path="/login" element={<Login setUser={setUser} />} />
          <Route path="/" element={<Home user={user} />} />
          <Route path="/requests" element={<Requests user={user} refreshUser={refreshUser} />} />
          <Route path="/chat/:bookingId" element={<Chat user={user} />} />
          <Route path="/post" element={<PostItem user={user} />} />
          <Route path="/leaderboard" element={<Leaderboard user={user} setUser={setUser} />} />
          <Route path="/profile" element={<Profile user={user} setUser={setUser} />} />

          <Route path="/settings" element={<Settings user={user} setUser={setUser} />} />
          <Route path="/thank-you" element={<ThankYou />} />
        </Routes>
      </main>
    </>
  );
}

export default App;
