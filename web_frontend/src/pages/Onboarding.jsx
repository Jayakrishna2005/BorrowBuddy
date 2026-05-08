import React, { useEffect, useState } from 'react';

function Onboarding({ onFinish }) {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    setTimeout(() => setVisible(true), 100);
  }, []);

  return (
    <div style={{
      display: 'flex', flexDirection: 'column', alignItems: 'center',
      minHeight: '100vh', background: 'white', padding: '2rem 1.5rem',
      opacity: visible ? 1 : 0, transition: 'opacity 1s ease', boxSizing: 'border-box'
    }}>
      <div style={{ marginTop: '3rem', textAlign: 'center', width: '100%', transform: visible ? 'translateY(0)' : 'translateY(20px)', transition: 'transform 1s ease' }}>
        <h1 style={{ fontSize: '2.2rem', fontWeight: 'bold', lineHeight: '1.2', margin: '0 0 1rem 0' }}>
          Share Today,<br />
          <span style={{ color: '#6C5CE7' }}>Help Forever 💜</span>
        </h1>
        <p style={{ fontSize: '1rem', color: 'gray', margin: 0 }}>
          Join your campus community and make a difference.
        </p>
      </div>

      <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', width: '100%', minHeight: '150px' }}>
        <div style={{ width: '120px', height: '120px', display: 'flex', justifyContent: 'center', alignItems: 'center', fontSize: '5rem', background: 'rgba(108, 92, 231, 0.1)', borderRadius: '50%', color: '#6C5CE7' }}>
          ✨
        </div>
      </div>

      <div style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: '1.5rem', marginBottom: '3rem' }}>
        <FeatureItem delay={200} icon="📚" color="#6C5CE7" title="Borrow Anything" desc="From books to gadgets" visible={visible} />
        <FeatureItem delay={400} icon="🛡️" color="#4CAF50" title="Verified Community" desc="Only students, always safe" visible={visible} />
        <FeatureItem delay={600} icon="🏆" color="#FF9800" title="Earn Rewards" desc="Help others and level up" visible={visible} />
      </div>

      <button 
        onClick={onFinish}
        className="btn"
        style={{
          width: '100%', padding: '1.2rem',
          background: 'linear-gradient(to right, #8E2DE2, #4A00E0)',
          color: 'white', fontSize: '1.1rem', borderRadius: '16px',
          border: 'none', fontWeight: 'bold', marginBottom: '1rem',
          boxShadow: '0 4px 15px rgba(108, 92, 231, 0.3)'
        }}
      >
        Get Started
      </button>

      <div style={{ color: 'gray', fontSize: '0.9rem', cursor: 'pointer' }} onClick={onFinish}>
        Already have an account? <span style={{ color: '#6C5CE7', fontWeight: 'bold' }}>Login</span>
      </div>
    </div>
  );
}

function FeatureItem({ icon, color, title, desc, delay, visible }) {
  const [show, setShow] = useState(false);
  useEffect(() => {
    if (visible) {
      setTimeout(() => setShow(true), delay);
    }
  }, [visible, delay]);

  return (
    <div style={{ 
      display: 'flex', alignItems: 'center', width: '100%',
      opacity: show ? 1 : 0, transform: show ? 'translateX(0)' : 'translateX(20px)', transition: 'all 0.8s ease'
    }}>
      <div style={{ 
        width: '48px', height: '48px', borderRadius: '50%', 
        background: `${color}20`, color: color,
        display: 'flex', justifyContent: 'center', alignItems: 'center', fontSize: '1.5rem'
      }}>
        {icon}
      </div>
      <div style={{ marginLeft: '1rem' }}>
        <div style={{ fontWeight: 'bold', fontSize: '1rem' }}>{title}</div>
        <div style={{ fontSize: '0.9rem', color: 'gray' }}>{desc}</div>
      </div>
    </div>
  );
}

export default Onboarding;
