document.addEventListener('DOMContentLoaded', function () {
    // Slideshow functionality (not used on payment page)
    let currentSlide = 0;
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

    // Password toggle functionality (not used on payment page)
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

    // Description modal functionality (not used on payment page)
    const descriptionButtons = document.querySelectorAll('.description-btn');
    const descriptionModal = document.getElementById('descriptionModal');
    const modalDescription = document.getElementById('modalDescription');
    const closeBtn = document.querySelector('.close-btn');

    if (descriptionButtons.length > 0 && descriptionModal && modalDescription && closeBtn) {
        descriptionButtons.forEach(button => {
            button.addEventListener('click', function () {
                modalDescription.textContent = this.getAttribute('data-description') || 'No description available.';
                descriptionModal.style.display = 'flex';
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

    // Initialize Flatpickr for the dueDate field (not used on payment page)
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

    // Dropdown menu functionality (used on payment page)
    const dropdownIcons = document.querySelectorAll('.dropdown-icon');
    dropdownIcons.forEach(icon => {
        icon.addEventListener('click', function (event) {
            event.preventDefault();
            event.stopPropagation();
            const dropdown = this.parentElement.nextElementSibling;
            if (dropdown && dropdown.classList.contains('action-dropdown')) {
                const isVisible = dropdown.classList.contains('active');
                document.querySelectorAll('.action-dropdown').forEach(d => d.classList.remove('active'));
                if (!isVisible) {
                    dropdown.classList.add('active');
                    const iconRect = icon.getBoundingClientRect();
                    const scrollTop = window.scrollY || document.documentElement.scrollTop;
                    dropdown.style.left = `${iconRect.left}px`;
                    dropdown.style.top = `${iconRect.bottom + scrollTop}px`;
                }
            }
        });
    });

    // Close dropdowns when clicking outside
    document.addEventListener('click', function (event) {
        const dropdowns = document.querySelectorAll('.action-dropdown');
        dropdowns.forEach(dropdown => {
            if (!dropdown.contains(event.target) && !event.target.classList.contains('dropdown-icon')) {
                dropdown.classList.remove('active');
            }
        });
    });

    // Action functionality (delete and archive) with custom modal (used on payment page)
    const actionLinks = document.querySelectorAll('.delete-link, .archive-link');
    const modal = document.getElementById('action-confirm-modal');
    const title = document.getElementById('action-title');
    const message = document.getElementById('action-message');
    const form = document.getElementById('action-form');
    const methodInput = document.getElementById('action-method');
    const cancelBtn = document.getElementById('cancel-action');

    if (actionLinks.length > 0 && modal && title && message && form && methodInput && cancelBtn) {
        actionLinks.forEach(link => {
            link.addEventListener('click', function (event) {
                event.preventDefault();
                const name = this.getAttribute('data-name');
                const id = this.getAttribute('data-id');
                const entityType = this.getAttribute('data-entity');
                const filterValue = this.getAttribute('data-filter');
                const isArchive = this.classList.contains('archive-link');

                title.textContent = isArchive ? 'Confirm Archiving' : 'Confirm Deletion';
                message.textContent = `Are you sure you would like to ${isArchive ? 'archive' : 'permanently delete'} the ${entityType} "${name}"?`;
                form.action = `/${entityType}s/${id}${isArchive ? '/archive' : ''}?filter=${filterValue}`;
                methodInput.value = isArchive ? 'PUT' : 'DELETE';

                modal.style.display = 'flex';
            });
        });

        cancelBtn.addEventListener('click', function () {
            modal.style.display = 'none';
        });

        window.addEventListener('click', function (event) {
            if (event.target === modal) {
                modal.style.display = 'none';
            }
        });
    }

    // Set default payment method functionality
    const defaultPaymentRadios = document.querySelectorAll('input[name="defaultPaymentMethod"]');
    defaultPaymentRadios.forEach(radio => {
        radio.addEventListener('change', function () {
            const paymentMethodId = this.value;
            fetch(`/payment-settings/set-default/${paymentMethodId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
                }
            }).then(response => {
                if (response.ok) {
                    window.location.reload(); // Reload the page to reflect the changes
                }
            });
        });
    });
});