
document.addEventListener("DOMContentLoaded", function() {
    PageUploader.uploadDetailPage();
    BtnUrlSetter.setReviewMoreBtn();
    BtnUrlSetter.setReserveBtn();
});

// 버튼 클릭 시 현재 url의 id 파라미터 전달하도록 속성 설정
// 대상 버튼: '예매자 한줄평 더보기', '예매하기'
const BtnUrlSetter = {
    setReviewMoreBtn : function() {
        let btn = document.querySelector(".btn_review_more");
        btn.setAttribute("href","./review?productId=" + getParam("productId") + "&displayInfoId=" + getParam("displayInfoId"));
    },
    setReserveBtn : function() {
        let btn = document.querySelector(".bk_btn");
		btn.setAttribute("onclick", "location.href='./booking?productId=" + getParam("productId") + "&displayInfoId=" + getParam("displayInfoId") + "'");
    }
}

/*
    상세보기 페이지 초기 실행되는 함수
     - 메인 슬라이드 로딩 
     - 탭 로딩 
     - 상세보기 탭 세팅
     - 리뷰어 정보 로딩
*/
const PageUploader = {
    uploadDetailPage : function() {
        let oReq = new XMLHttpRequest();
        oReq.addEventListener("load", function() {
            if (oReq.status === 200) {
                let displayInfoSet = JSON.parse(oReq.responseText);

                SlideSetter.loadSlide(displayInfoSet);
                DetailTabs.loadDetailTabs(displayInfoSet);
                DetailBtn.setDetail(displayInfoSet);
                ReviewSetter.setReview(displayInfoSet);

                PageUploader.setProductCookie(displayInfoSet);
            }
        });

        let displayInfoId = getParam("displayInfoId");
        url = "./api/products/" + displayInfoId;
        oReq.open("GET", url);
        oReq.send();
    },
    setProductCookie : function(displayInfoSet) {
        setCookie('productId', displayInfoSet.productPriceId[0].productId);
        setCookie('productPriceId', displayInfoSet.productPriceId[0].productPriceId);
        setCookie('displayInfoId', displayInfoSet.displayInfo.displayInfoId);
    }
};

const SlideSetter = {
    loadSlide : function(displayInfoSet) {
        this.setSlideImages(displayInfoSet);
        this.setSlideMovingImage();
    },
    // 모든 슬라이드 슬라이드 이미지 등록
    setSlideImages : function(displayInfoSet) {
        let slider = document.querySelector(".container_visual .visual_img");
        let detailSlideTemplate = document.querySelector("#detail-slide-template").innerHTML;
        let bindTemplate = Handlebars.compile(detailSlideTemplate);

        let data = {
            "productDescription" : displayInfoSet.displayInfo.productDescription,
            "productImages" : displayInfoSet.productImages
        };

        slider.innerHTML = bindTemplate(data);
    },
    // 슬라이드 움직임 기능 구현, prev와 nxt 버튼 구현
    setSlideMovingImage : function() {
        const slideWidth = 414;
        let images = document.querySelector(".visual_img");
        let totalSlideCnt = images.childElementCount;
        let prevBtn = document.querySelector(".prev");
        let nxtBtn = document.querySelector(".nxt");
        let pagination = document.querySelector(".figure_pagination");

        if(totalSlideCnt < 2) {
            prevBtn.removeChild(document.querySelector(".prev_inn"));
            nxtBtn.removeChild(document.querySelector(".nxt_inn"));
            pagination.removeChild(document.querySelector(".off"));
            return;
        }

        prevBtn.addEventListener("click", function(){
            let currentPage = document.querySelector(".figure_pagination").firstElementChild.innerHTML;
            --currentPage;
            if(currentPage === 0) {
                currentPage = totalSlideCnt;
            }

            images.style.transform = "translate(-" + (slideWidth * (currentPage-1)) + "px,0)";

            let pagination = SlideSetter.setPagination.bind(this.setPagination, "prev");
            pagination();
        });

        nxtBtn.addEventListener("click", function(){
            let currentPage = parseInt(document.querySelector(".figure_pagination").firstElementChild.innerHTML);
            if(currentPage === totalSlideCnt) {
                currentPage = 0;
            }
            images.style.transform = "translate(-" + (slideWidth * currentPage) + "px,0)";

            let pagination = SlideSetter.setPagination.bind(this.setPagination, "nxt");
            pagination();
        });
    },
    // 슬라이드 이미지의 세부 동작 구현
    setPagination : function(clickedBtn) {
        let pagination = document.querySelector(".figure_pagination");
        let currentPage = pagination.firstElementChild.innerHTML;
        let nextPage = document.querySelector(".off").firstElementChild.innerHTML;
        let maxPageCnt = document.querySelector(".visual_img").childElementCount + "";

        if(clickedBtn === "prev") {
            currentPage--;
            if(currentPage === 0) {
                document.querySelector(".off").firstElementChild.innerHTML = 1;
                pagination.firstElementChild.innerHTML = maxPageCnt;
            } else {
                document.querySelector(".off").firstElementChild.innerHTML = currentPage + 1;
                pagination.firstElementChild.innerHTML = currentPage;
            }
        } else if(clickedBtn === "nxt"){
            pagination.firstElementChild.innerHTML = nextPage;
            if(nextPage === maxPageCnt) {
                document.querySelector(".off").firstElementChild.innerHTML = 1;
            } else {
                document.querySelector(".off").firstElementChild.innerHTML = ++nextPage;
            }
        }
    }
}

// 펼쳐보기 기능 
//  - 새로고침 없이 동작할 수 있도록 구현 
const DetailBtn = {
    setDetail : function(displayInfoSet) {
        let dsc = document.querySelector(".dsc");
        dsc.innerHTML = displayInfoSet.displayInfo.productContent;
        this.open();
        this.close();
    },
    open : function() {
        let open = document.querySelector("._open");
        let close = document.querySelector("._close");
        open.addEventListener("click", function(){
            document.querySelector(".store_details").className = "store_details";
            open.style.display = "none";
            close.style.display = "";
        });
    },
    close : function() {
        let open = document.querySelector("._open");
        let close = document.querySelector("._close");
        close.addEventListener("click", function(){
            document.querySelector(".store_details").className = "store_details close3";
            open.style.display = "";
            close.style.display = "none";
        });
    }
}

// 3명 리뷰어 코멘트 등록
const ReviewSetter = {
    setReview : function(displayInfoSet){
        this.updateReviewHeader(displayInfoSet);
        this.updateReview(displayInfoSet);
    },
    updateReviewHeader : function(displayInfoSet) {
        let averageStar = document.querySelector(".graph_value");
        let averageScore = document.querySelector(".grade_area > .text_value > span"); 
        let totalReviewCount = document.querySelector(".grade_area > .join_count > em");

        averageStar.style.width = 100 * displayInfoSet.averageScore.toFixed(1) / 5.0 + "%"; 
        averageScore.innerHTML = displayInfoSet.averageScore.toFixed(1);
        totalReviewCount.innerHTML = displayInfoSet.comments.length +"건";
    },
    updateReview : function(displayInfoSet) {
        let reviewBox = document.querySelector(".list_short_review");
        let reviewTemplate = document.querySelector("#review-template").innerHTML;
        let bindTemplate = Handlebars.compile(reviewTemplate);

        let data = {
                displayInfo : displayInfoSet.displayInfo,
                comments :  displayInfoSet.comments.slice(0,3)
        };

        Handlebars.registerHelper('filePath', function(item) {
            return item.commentImages[0].saveFileName;
        });

        Handlebars.registerHelper('imgCnt', function(item) {
            return item.length;
        });

        Handlebars.registerHelper('commentTime', function(item) {
            let time = new Date(item.reservationDate);
            return toStringByFormatting(time);
        });

        function toStringByFormatting(source) {
            const delimiter = '-';
            const year = source.getFullYear();
            const month = leftPad(source.getMonth() + 1);
            const day = leftPad(source.getDate());
        
            return [year, month, day].join(delimiter);
        }

        function leftPad(value) {
            if (value >= 10) {
                return value;
            }
        
            return `0${value}`;
        }

        reviewBox.innerHTML  = bindTemplate(data);
    }
}

/*
    '상세정보', '오시는 길' 탭 active 설정
     - loadDetailTabs: 상세페이지 탭들 로등(초기화, eventListener등록, 상세 내용 추가)
     - setActive: 상세정보, 오시는 길 탭 눌렀을 때 반응 처리
     - setProductContent: 상세정보 탭의 소개 내용 추가
*/
const DetailTabs = {
    loadDetailTabs : function(displayInfoSet) {
        this.setActive(displayInfoSet);
        this.setProductContent(displayInfoSet);
    },
    setActive : function(displayInfoSet) {
        let detailTab = document.querySelector("._detail");
        let detailTabClasses = detailTab.children[0].className;
        let pathTab = document.querySelector("._path");
        let pathTabClasses = pathTab.children[0].className;
        let detailInfoGrp = document.querySelector(".detail_info_group");
        
        setPathInfo(displayInfoSet.displayInfo);

        detailTab.addEventListener("click", function() {
            detailInfoGrp.children[0].style.display = "";
            detailInfoGrp.children[1].style.display = "";
            document.querySelector(".detail_location").className += " hide";

            if(!detailTabClasses.includes("active")) {
                detailTabClasses = detailTabClasses.replace(" ", "");
                detailTabClasses += " active";
                detailTab.children[0].className = detailTabClasses;
            }

            if(pathTabClasses.indexOf("active") !== -1) {
                pathTabClasses = pathTabClasses.substring(0, pathTabClasses.indexOf("active"));
                pathTab.children[0].className = pathTabClasses;
            }
        });

        pathTab.addEventListener("click", function() {
            detailInfoGrp.children[0].style.display = "none";
            detailInfoGrp.children[1].style.display = "none";
            document.querySelector(".detail_location").className = "detail_location";

            if(!pathTabClasses.includes("active")) {
                pathTabClasses = pathTabClasses.replace(" ", "");
                pathTabClasses += " active";
                pathTab.children[0].className = pathTabClasses;
            }

            if(detailTabClasses.indexOf(" active") !== -1) {
                detailTabClasses = detailTabClasses.substring(0, detailTabClasses.indexOf("active"));
                detailTab.children[0].className = detailTabClasses;
            }
        });

        function setPathInfo(info) {
            document.querySelector(".store_name").innerHTML = info.placeName;
            document.querySelector(".store_addr").innerHTML = info.placeStreet;
            document.querySelector(".addr_old_detail").innerHTML = info.placeLot;
            document.querySelector(".addr_detail").innerHTML = info.placeStreet;
            document.querySelector(".store_tel").innerHTML = info.telephone;
            document.querySelector(".store_tel").setAttribute("href","tel:" + info.telephone);
        }
    },
    setProductContent : function(displayInfoSet) {
        let detailIntro = document.querySelector(".detail_info_lst > .in_dsc");
        detailIntro.innerHTML = displayInfoSet.displayInfo.productContent;
    }
}

