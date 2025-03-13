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
            dateFormat: "Y-m-d",
            altInput: true,
            altFormat: "m/d/Y",
            defaultDate: dueDateInput.value || "today",
            minDate: "today",
            allowInput: false,
        });
    }

    // Dropdown menu functionality
    const dropdownIcons = document.querySelectorAll('.dropdown-icon');
    const taskDropdowns = document.querySelectorAll('.task-dropdown');

    dropdownIcons.forEach((icon, index) => {
        const dropdown = taskDropdowns[index];
        icon.addEventListener('click', function (event) {
            event.preventDefault();
            event.stopPropagation();

            // Get the position of the arrow
            const rect = icon.getBoundingClientRect();
            const scrollTop = window.pageYOffset || document.documentElement.scrollTop;

            // Position the dropdown with its top-left corner at the arrow's bottom-left
            dropdown.style.left = `${rect.left}px`;
            dropdown.style.top = `${rect.bottom + scrollTop}px`;

            dropdown.classList.toggle('active');

            // Close other dropdowns if open
            taskDropdowns.forEach((otherDropdown, otherIndex) => {
                if (otherDropdown !== dropdown && otherDropdown.classList.contains('active')) {
                    otherDropdown.classList.remove('active');
                }
            });
        });
    });

    // Close dropdown when clicking outside
    document.addEventListener('click', function (event) {
        taskDropdowns.forEach(dropdown => {
            if (!dropdown.contains(event.target) && !event.target.classList.contains('dropdown-icon')) {
                dropdown.classList.remove('active');
            }
        });
    });

    // Delete functionality (placeholder)
    const deleteLinks = document.querySelectorAll('.delete-link');
    deleteLinks.forEach(link => {
        link.addEventListener('click', function (event) {
            event.preventDefault();
            const taskId = this.getAttribute('data-task-id');
            if (confirm(`Are you sure you want to delete task with ID ${taskId}?`)) {
                console.log(`Delete task with ID: ${taskId}`);
                this.closest('tr').remove();
            }
        });
    });
});