window.addEventListener('DOMContentLoaded', initialize);

function initialize() {
	fetchReservation();
}

function fetchReservation() {
	httpRequest('GET', '/reservations?reservationEmail=' + getCookie('reservationEmail'), onLoadHandler);
}

function onLoadHandler(responseString) {
const { size, reservations } = JSON.parse(responseString);

	if(size === 0) {
		document.querySelector('.err').style.display = '';
		return;
	}
	const classifiedReservations = classifyReservations();
	renderStatus();
	renderReservations();
	renderReservationCount();
	hideWithNoContent();
	setEventListeners();

	function classifyReservations() {
		const classifiedReservations = { confirmed: [], used: [], canceled: [] };

		reservations.forEach(reservation => {
			let status = 'confirmed';
			if(reservation.cancelYn) {
				status = 'canceled';
			} else if(isCompletedReservation(reservation.reservationDate)) {
				status = 'used';
			}

			classifiedReservations[status].push(reservation);
		});

		return classifiedReservations;
	}

	function isCompletedReservation(reservationDate) {
		const reservedDate = new Date(reservationDate);
		return reservedDate < new Date();
	}

	function renderStatus() {
		const template = document.getElementById('cardItem').textContent;
		const listCards = document.querySelector('.list_cards');
		const status = [
			{
				statusText: '예약 확정',
				statusIcon: 'ico_clock',
				status: 'confirmed'
			},
			{
				statusText: '이용 완료',
				statusIcon: 'ico_check2',
				status: 'used'
			},
			{
				statusText: '취소된 예약',
				statusIcon: 'ico_cancel',
				status: 'canceled'
			}
		];

		listCards.innerHTML = status.reduce((acc, cur) => {
			const { status, statusText, statusIcon } = cur;

			return acc + template.replace("{status}", status)
					.replace("{statusText}", statusText)
					.replace("{statusIcon}", statusIcon)
		}, '');
	}

	function renderReservations() {
		const statusCards = document.querySelectorAll('.list_cards > li');
		const template = document.getElementById('reservationItem').textContent;

		for(const status in classifiedReservations) {
			const reservationList = classifiedReservations[status];

			statusCards[getIndex(status)].innerHTML += reservationList.reduce((acc, cur) => {
				const {
					reservationInfoId,
					reservationDate,
					totalPrice,
					displayInfo
				} = cur;

				return acc + template.replace(/\{reservationInfoId\}/g, reservationInfoId)
									 .replace("{reservationDate}", reservationDate)
									 .replace("{productDescription}", displayInfo.productDescription)
									 .replace("{placeStreet}", displayInfo.placeStreet)
									 .replace("{productContent}", displayInfo.productContent)
									 .replace("{totalPrice}", totalPrice.toLocaleString())
									 .replace("{homepage}", displayInfo.homepage)
									 .replace("{buttonClass}", getButtonClass(status))
									 .replace("{buttonText}", getButtonText(status));
			}, '');
		}
	}

	function renderReservationCount() {
		renderCount('.total', size);
		for(const status in classifiedReservations) {
			renderCount(`.${status}`, classifiedReservations[status].length);
		}
		function renderCount(status, count) {
			const figure = document.querySelector(`.link_summary_board${status} .figure`)
			if(figure) {
				figure.textContent = count;
			}
		}
	}

	function hideWithNoContent() {
		for(const status in classifiedReservations) {
			if(classifiedReservations[status].length === 0) {
				const index = getIndex(status);
				const statusCards = document.querySelectorAll('.list_cards > li');
				statusCards[index].style.display = 'none';
			}
		}
	}

	function getIndex(status) {
		if(status === 'confirmed') {
			return 0;
		} else if(status === 'used') {
			return 1;
		}
		return 2;
	}

	function getButtonText(status) {
		if(status === 'confirmed') {
			return '취소';
		} else if(status === 'used') {
			return '예매자 리뷰 남기기';
		}
		return '이용 완료';
	}

	function getButtonClass(status) {
		if(status === 'confirmed') {
			return 'cancel';
		} else if(status === 'used') {
			return 'comment';
		}
		return 'disable';
	}

	function setEventListeners() {
		moveCommentPage();
		cancelReservation();

		function moveCommentPage() {
			document.querySelectorAll('.btn.comment').forEach(btn => {
				btn.addEventListener('click', function() {
					localStorage.setItem('reservationInfoId', this.dataset.reservationinfoid);
					location.href="./reviewWrite";
				});
			});
		}

		function cancelReservation() {
			document.querySelectorAll('.btn.cancel').forEach(btn => {
				btn.addEventListener('click', function() {
                    if (confirm('취소하시겠습니까?')) {
                        httpRequest("PUT", `/reservations/${this.dataset.reservationinfoid}`, changeReservation);
                    }
				});
			});
		}

        function changeReservation() {
            location.reload();
        }
	}
}