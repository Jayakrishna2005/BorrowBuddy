import React, { useEffect, useState } from 'react';

function Splash({ onFinish }) {
  const [opacity, setOpacity] = useState(0);
  const [scale, setScale] = useState(0.85);

  useEffect(() => {
    // Entrance animation
    setTimeout(() => {
      setOpacity(1);
      setScale(1);
    }, 100);

    // Leave splash
    const timer = setTimeout(() => {
      onFinish();
    }, 800); // Drastically reduced for fast app load

    return () => clearTimeout(timer);
  }, [onFinish]);

  return (
    <div style={{ 
      display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center',
      minHeight: '100vh', 
      background: 'linear-gradient(to bottom, #0A0F2C, #020617)', 
      position: 'relative', 
      overflow: 'hidden'
    }}>
      <style>
        {`
          @keyframes subtleFloat {
            0% { transform: translateY(0px); filter: drop-shadow(0 0 15px rgba(123, 97, 255, 0.3)); }
            50% { transform: translateY(-8px); filter: drop-shadow(0 0 30px rgba(0, 209, 178, 0.4)); }
            100% { transform: translateY(0px); filter: drop-shadow(0 0 15px rgba(123, 97, 255, 0.3)); }
          }
        `}
      </style>

      <div style={{
        display: 'flex', flexDirection: 'column', alignItems: 'center',
        opacity: opacity, 
        transform: `scale(${scale})`, 
        transition: 'all 1.5s cubic-bezier(0.25, 1, 0.5, 1)'
      }}>
        <img 
          src="/logo.png" 
          alt="BorrowBuddy Logo" 
          style={{ 
            width: '320px', 
            height: '320px', 
            objectFit: 'contain',
            animation: 'subtleFloat 4s ease-in-out infinite',
            borderRadius: '40px'
          }} 
        />
      </div>

      <div style={{ 
        position: 'absolute', 
        bottom: '48px', 
        display: 'flex', 
        flexDirection: 'column', 
        alignItems: 'center', 
        opacity: opacity, 
        transition: 'opacity 1.5s ease 1s' 
      }}>
        <div className="loader" style={{ borderColor: 'rgba(255, 255, 255, 0.1)', borderTopColor: '#00D1B2', marginBottom: '24px' }}></div>
        <div style={{ fontSize: '0.75rem', fontWeight: 'bold', color: 'rgba(255, 255, 255, 0.5)', letterSpacing: '3px' }}>
          POWERED BY BORROWBUDDY
        </div>
      </div>
    </div>
  );
}

export default Splash;
