document.addEventListener("DOMContentLoaded", function() {
    CategoryLoader.init();
    ProductLoader.loadProducts(0, 0);
});

const ProductLoader = {
    isFetching: false, // 중복 요청 방지

    loadProducts: function(categoryId, start) {
        if (this.isFetching) return;
        this.isFetching = true;

        fetch(`/api/products?categoryId=${categoryId}&start=${start}`)
            .then(response => {
                if (!response.ok) throw new Error(`HTTP error! Status: ${response.status}`);
                return response.json();
            })
            .then(data => {
                this.renderProducts(data);
                this.isFetching = false;
            })
            .catch(error => {
                console.error("Error fetching products:", error);
                this.isFetching = false;
            });
    },

    renderProducts: function(data) {
        const productList = document.getElementById("productList");
        const totalCount = document.getElementById("totalCount");

        totalCount.innerText = data.totalProductCount;
        productList.innerHTML = ""; // 기존 목록 초기화

        data.products.forEach(product => {
            const listItem = document.createElement("li");
            listItem.className = "item";
            listItem.innerHTML = `
                <div class="event_txt">
                    <h4 class="event_txt_tit"><span>${product.title}</span></h4>
                    <p class="event_txt_dsc">${product.description || "설명 없음"}</p>
                    <p class="release_date">출시일: ${product.releaseDate}</p>
                    <p class="running_time">상영시간: ${product.runningTime}분</p>
                </div>
            `;
            productList.appendChild(listItem);
        });
    }
};

const CategoryLoader = {
    init: function() {
        const categoryTabs = document.getElementById("categoryTabs");

        categoryTabs.addEventListener("click", function(event) {
            const target = event.target.closest("li"); // 클릭된 요소가 li 안에 있는지 확인
            if (!target) return;

            const categoryId = target.getAttribute("data-category");

            // 모든 탭에서 active 제거 후 선택된 탭에 추가
            document.querySelectorAll(".event_tab_lst .item").forEach(tab => tab.classList.remove("active"));
            target.classList.add("active");

            // 제품 목록 새로 로드 (start index 0으로 초기화)
            ProductLoader.loadProducts(categoryId, 0);
        });
    }
};
