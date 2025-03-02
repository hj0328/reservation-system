document.addEventListener('DOMContentLoaded', function () {
    ButtonMyBooking.init();
});

const ButtonMyBooking = {
    init: function () {
        let button = document.querySelector('button');
        button.addEventListener('click', function () {
            let email = document.querySelector('input').value;
            if (!ButtonMyBooking.isValidEmail(email)) {
                alert('유효하지 않은 이메일주소입니다.');
                document.querySelector('form').action = '';
                return;
            }

            document.querySelector('form').action = './booking-login';
        });
    },
    isValidEmail: function (bookEmail) {
        let isEmailValid = bookEmail.match(/^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*\.[a-zA-Z]{2,3}$/);
        return isEmailValid;
    }
}
