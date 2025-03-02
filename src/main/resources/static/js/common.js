
document.addEventListener("DOMContentLoaded", function() {
    moveTop();
  });

function moveTop() {
    let lnkTop = document.querySelector(".lnk_top");
    lnkTop.addEventListener("click", function() {
        window.scrollTo({
            top:0,
            left:0,
            behavior:'smooth'});
    });
}

function getParam(key) {
    let params = location.search.slice(1);
    params = params.split("&");

    let value = "";
    params.forEach(function(param){
        let paramArr = param.split("=");
        if(paramArr[0] === key) value += paramArr[1];
    });

    return value;
}

function setCookie(cookieName, value) {
    document.cookie = cookieName + '=' + value;
}

function getCookie(cookieName) {
    var x, y;
    var val = document.cookie.split(';');

    for (var i = 0; i < val.length; i++) {
      x = val[i].substring(0, val[i].indexOf('='));
      y = val[i].substring(val[i].indexOf('=') + 1);
      x = x.replace(/^\s+|\s+$/g, ''); // 앞과 뒤의 공백 제거하기
      if (x == cookieName) {
        return decodeURI(y); // unescape로 디코딩 후 값 리턴
      }
    }
  }

  function httpRequest(method, url, callback, payload) {
    const baseURL = 'http://localhost:8080/api';
     const xhr = new XMLHttpRequest();

    xhr.onload = function() {
      if(xhr.status === 200) {
        if (callback !== undefined) {
          callback(xhr.response);
        }
      }
    };
    xhr.open(method, baseURL + url);

    if(payload instanceof FormData) {
      xhr.send(payload);
    } else if(payload) {
      xhr.send(JSON.stringify(payload));
    } else {
      xhr.send();
    }
  }