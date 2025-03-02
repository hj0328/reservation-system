
document.addEventListener("DOMContentLoaded", function() {
    PageUploader.uploadDetailPage();
    BtnBackSetter.setBtnBack();
});

const BtnBackSetter = {
    setBtnBack : function() {
        let btn = document.querySelector(".btn_back");
        btn.setAttribute("href","./detail?productId=" + getParam("productId") + "&displayInfoId=" + getParam("displayInfoId"));
    }
}

const PageUploader = {
    uploadDetailPage : function() {
        let oReq = new XMLHttpRequest();
        oReq.addEventListener("load", function() {
            if (oReq.status === 200) {
                let displayInfoSet = JSON.parse(oReq.responseText);
                ReviewSetter.setReview(displayInfoSet);
            }
        });

        let displayId = getParam("productId");
        url = "./api/products/" + displayId;
        oReq.open("GET", url);
        oReq.send();
    }
};

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
                comments :  displayInfoSet.comments
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