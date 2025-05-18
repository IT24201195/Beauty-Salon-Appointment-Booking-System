
document.addEventListener('DOMContentLoaded', function() {
    // ====== NAVIGATION ======
    // Smooth scrolling for navigation links - improves user experience when clicking nav links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href');
            
            if(targetId === '#') return; // Skip if it's just "#" (home link)
            
            const targetElement = document.querySelector(targetId);
            if(targetElement) {
                window.scrollTo({
                    top: targetElement.offsetTop - 70, // Account for fixed header height
                    behavior: 'smooth'
                });
            }
        });
    });

    // ====== BOOKING FORM ======
    // Set minimum date for booking (today) - prevents users from selecting past dates
    const dateInput = document.getElementById('date');
    if(dateInput) {
        const today = new Date();
        const dd = String(today.getDate()).padStart(2, '0');
        const mm = String(today.getMonth() + 1).padStart(2, '0'); // January is 0!
        const yyyy = today.getFullYear();
        
        const todayFormatted = yyyy + '-' + mm + '-' + dd;
        dateInput.setAttribute('min', todayFormatted);
    }

    // Basic form validation - ensures booking times are within business hours
    const bookingForm = document.querySelector('.booking-section form');
    if(bookingForm) {
        bookingForm.addEventListener('submit', function(e) {
            const timeInput = document.getElementById('time');
            if(timeInput) {
                const selectedTime = timeInput.value;
                const hour = parseInt(selectedTime.split(':')[0]);
                
                // Check if selected time is within business hours (9AM - 7PM)
                if(hour < 9 || hour >= 19) {
                    e.preventDefault();
                    alert('Please select a time between 9:00 AM and 7:00 PM');
                }
            }
        });
    }

    // ====== UI ENHANCEMENTS ======
    // Service card hover effect - provides visual feedback when hovering over services
    const serviceCards = document.querySelectorAll('.service-card');
    serviceCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.backgroundColor = '#fff8f8'; // Light pink background on hover
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.backgroundColor = '#ffffff'; // Back to white
        });
    });

}); 
