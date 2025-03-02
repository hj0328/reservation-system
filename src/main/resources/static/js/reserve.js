document.addEventListener('DOMContentLoaded', function () {
    ButtonBackSetter.setButtonBack();

    const TICKET_TYPE_COUNT = 4;
    let quantityList = document.querySelectorAll('.qty');
    for (let i = 0; i < TICKET_TYPE_COUNT; i++) {
        quantityList[i].addEventListener('click', function (evt) {
            CountButton.countTicket(evt);
            Reserve.isPass();
        });
    }

    BookingForm.init();
    AgreeButton();
    Reserve.init();
});

const ButtonBackSetter = {
    setButtonBack: function () {
        let button = document.querySelector('.btn_back');
        button.setAttribute('href', './detail?productId=' + getParam('productId') + "&displayInfoId=" + getParam("displayInfoId"));
    }
}

/*
    티켓수 선택 기능
    + 버튼 클릭 시 개수와 전체 가격 증가(-버튼 활성화)
    - 버튼 클릭 시 개수와 전체 가격 감소(개수가 0되면 -버튼 비활성화)
*/
const CountButton = {
    countTicket: function (evt) {
        if (evt.target.className.includes('ico_minus3')) {
            this.setMinusCount(evt);
        } else if (evt.target.className.includes('ico_plus3')) {
            this.setPlusCount(evt);
        }
    },
    setMinusCount: function (evt) {
        let currentCount = evt.target.nextElementSibling.value;
        if (currentCount === '0') {
            return;
        }

        currentCount--;
        evt.target.nextElementSibling.setAttribute('value', currentCount);

        if (currentCount === 0) {
            evt.target.classList.toggle('disabled');
            evt.target.nextElementSibling.classList.toggle('disabled');
        }

        let price = evt.currentTarget.getElementsByClassName('price')[0].innerText.replace(',', '');
        let totalPrice = evt.currentTarget.getElementsByClassName('total_price')[0].innerHTML;
        evt.currentTarget.getElementsByClassName('total_price')[0].innerHTML = Number(totalPrice) - Number(price);
        this.countTotalTicket(0, 1);
    },
    setPlusCount: function (evt) {
        let currentCount = evt.target.previousElementSibling.value;
        if (currentCount === '0') {
            evt.target.previousElementSibling.classList.toggle('disabled');
            evt.target.previousElementSibling.previousElementSibling.classList.toggle('disabled');
        }

        currentCount++;
        evt.target.previousElementSibling.setAttribute('value', currentCount);

        let price = evt.currentTarget.getElementsByClassName('price')[0].innerText.replace(',', '');
        let totalPrice = evt.currentTarget.getElementsByClassName('total_price')[0].innerHTML;
        evt.currentTarget.getElementsByClassName('total_price')[0].innerHTML = Number(totalPrice) + Number(price);
        this.countTotalTicket(1, 0);
    },
    countTotalTicket: function (addCount, minusCount) {
        let totalCount = document.getElementById('totalCount').innerHTML;
        document.getElementById('totalCount').innerHTML = Number(totalCount) + addCount - minusCount;
    }
}

/*
    예매자 정보 검증
     - 휴대전화, 집전화 검증
*/
const BookingForm = {
    init: function () {
        let bookingForm = document.querySelector('.form_horizontal');
        bookingForm.addEventListener('change', this.checkBookInput);

        let bookInputs = document.querySelectorAll('.inline_control > input');
        bookInputs.forEach(function (bookinput) {
            bookinput.addEventListener('change', this.checkBookInput);
        });
    },
    checkBookInput: function (evt) {
        if (evt.target.id === 'tel') {
            let isMatch = evt.target.value.match(/01[01789]-\d{3,4}-\d{4}|\d{2,3}-\d{3,4}-\d{4}$/);
            if (isMatch === null) {
                evt.target.value = '';
                alert('옳바른 휴대전화 형식을 입력해주세요. \n예시) 010-0000-0000');
                return;
            }
        }
    }
}

/*
    동의 버튼 기능
     - 접기, 보기 토글
*/
function AgreeButton() {
    let agreeButtons = document.querySelectorAll('.agreement');
    agreeButtons.forEach(button => {
        button.addEventListener('click', function (evt) {
            if (!evt.currentTarget.classList.contains('all')) {
                AgreeButton.prototype.toggle(evt);
            }
        })
    });
}

AgreeButton.prototype = {
    toggle: function (evt) {
        let agreeView = evt.currentTarget.classList;
        if (agreeView.contains('open')) {
            agreeView.remove('open');
        } else {
            agreeView.add('open');
        }
        let agreeArrow = evt.currentTarget.children[1].children[1].classList;
        if (agreeArrow.contains('fn-down2')) {
            agreeArrow.remove('fn-down2');
            agreeArrow.add('fn-up2');
        } else {
            agreeArrow.remove('fn-up2');
            agreeArrow.add('fn-down2');
        }
    }
}

/*
    '예약하기 버튼' 활성화 기능
     - input 태그 변화 감지할 때마다 유효성 점검
     - isPass(): 유효성 통과 못하면 '예약하기 버튼' 비활성화
                 유효성 통과하면, 활성화
     - 유효여부: 사용자 모든 입력값 정상 입력(이메일도 유효성 체크), 예매 수 최소 1매, 이용자 약과 동의 버튼 체크 활성화

    setSendPost() 기능
    개인적으로 추가로 넣은 코드입니다.
    기획에서 '예약하기' 이후 디비에 저장되지 않지만,
    '예약확인'기능을 바로 확인하고자 Post, Redirect, Get 패턴으로 메인화면으로 이동하도록 하였습니다.
*/
const Reserve = {
    init: function () {
        let inputs = document.querySelectorAll('input');
        inputs.forEach(input => {
            input.addEventListener('change', function () {
                Reserve.isPass();
            });
        });

        Reserve.setSendPost();
    },
    isPass: function () {
        let totalCount = document.querySelector('#totalCount').innerHTML;
        if (totalCount === '0') {
            // 버튼 비활성화
            if (!document.querySelector('.bk_btn_wrap').classList.contains('disable')) {
                document.querySelector('.bk_btn_wrap').classList.add('disable')
            }
            return;
        }

        let bookName = document.querySelector('#name').value;
        let bookTel = document.querySelector('#tel').value;
        let bookEmail = document.querySelector('#email').value;
        let isCheked = document.querySelector('input[id=chk3]:checked');

        if (Reserve.checkFail(bookName, bookTel, bookEmail, isCheked)) {
            // 버튼 비활성화
            if (!document.querySelector('.bk_btn_wrap').classList.contains('disable')) {
                document.querySelector('.bk_btn_wrap').classList.add('disable')
            }
            return;
        }

        document.querySelector('.bk_btn_wrap').classList.remove('disable');
    },
    checkFail: function (bookName, bookTel, bookEmail, isCheked) {
        let isEmailValid = bookEmail.match(/^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*\.[a-zA-Z]{2,3}$/);
        return !bookName || !bookTel || !isEmailValid || !isCheked
    },
    setSendPost: function () {
        document.querySelector('.bk_btn_wrap').addEventListener('click', function () {
            if (!document.querySelector('.bk_btn_wrap').classList.contains('disable')) {

                let date = document.querySelector('.inline_txt.selected').innerText.split(' ')[0];
                let prices = [
                    {
                        "count": Number(document.querySelector('#totalCount').innerText),
                        productPriceId: getCookie('productPriceId'),
                        "reservationInfoId": 1,
                        "reservationInfoPriceId": 16
                    }
                ];
                let reqJson = {
                    'productId': getCookie('productId'),
                    'displayInfoId': getCookie('displayInfoId'),
                    "reservationEmail": document.querySelector('#email').value,
                    "reservationName": document.querySelector('#name').value,
                    "reservationTelephone": document.querySelector('#tel').value,
                    "reservationYearMonthDay": date,
                    "prices": prices
                };

                let oReq = new XMLHttpRequest();
                oReq.addEventListener("load", function(response) {
                    if (oReq.status === 200) {
                        window.location.href = "./my-reservation";
                    }
                });

                url = "./my-reservation";
                oReq.open("POST", url);
                oReq.setRequestHeader('Content-Type', 'application/json');
                oReq.send(JSON.stringify(reqJson));
            }
        });
    },
    formatDate: function(date, format) {
        return format.replace('mm', String(date.getMonth() + 1).padStart(2, '0'))
            .replace('yyyy', date.getFullYear())
            .replace('dd', String(date.getDate()).padStart(2, '0'));
    }
}

