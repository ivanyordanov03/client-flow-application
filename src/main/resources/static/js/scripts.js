let currentSlide = 0;

document.addEventListener('DOMContentLoaded', function () {

    // Slideshow functionality
    const slides = document.querySelectorAll('.slide');
    const dots = document.querySelectorAll('.dot');

    if (slides.length > 0 && dots.length > 0) {
        function goToSlide(index) {
            slides.forEach((slide) => slide.classList.remove('active'));
            dots.forEach((dot) => dot.classList.remove('active'));
            slides[index].classList.add('active');
            dots[index].classList.add('active');
            currentSlide = index;
        }

        function nextSlide() {
            currentSlide = (currentSlide + 1) % slides.length;
            goToSlide(currentSlide);
        }

        goToSlide(0);
        setInterval(nextSlide, 5000);
    }

    // Password toggle functionality
    const togglePassword = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('password');
    if (togglePassword && passwordInput) {
        togglePassword.addEventListener('click', function () {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            this.querySelector('i').classList.toggle('fa-eye');
            this.querySelector('i').classList.toggle('fa-eye-slash');
        });
    }

    // Terms checkbox functionality
    const termsCheckbox = document.getElementById('termsCheckbox');
    const submitButton = document.getElementById('submitPayment');
    if (termsCheckbox && submitButton) {
        submitButton.disabled = !termsCheckbox.checked;
        termsCheckbox.addEventListener('change', function () {
            submitButton.disabled = !this.checked;
        });
    }

    // Description modal functionality
    const descriptionButtons = document.querySelectorAll('.description-btn');
    const descriptionModal = document.getElementById('descriptionModal');
    const modalDescription = document.getElementById('modalDescription');
    const closeBtn = document.querySelector('.close-btn');

    if (descriptionButtons.length > 0 && descriptionModal && modalDescription && closeBtn) {
        descriptionButtons.forEach(button => {
            button.addEventListener('click', function () {
                modalDescription.textContent = this.getAttribute('data-description') || 'No description available.';
                descriptionModal.style.display = 'block';
            });
        });

        closeBtn.addEventListener('click', function () {
            descriptionModal.style.display = 'none';
        });

        window.addEventListener('click', function (event) {
            if (event.target === descriptionModal) {
                descriptionModal.style.display = 'none';
            }
        });
    }

    // Initialize Flatpickr for the dueDate field
    const dueDateInput = document.getElementById('dueDate');
    if (dueDateInput) {
        flatpickr("#dueDate", {
            dateFormat: "m/d/Y",
            altInput: true,
            altFormat:"Y-m-d",
            defaultDate: dueDateInput.value,
            minDate: "today",
            allowInput: false,
        });
    }
});