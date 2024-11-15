document.addEventListener("mousemove", function(e) {
    let speed = document.querySelectorAll(".parallax");
    speed.forEach(function(el) {
        let x = (window.innerWidth - e.pageX * 5) / 100;
        let y = (window.innerHeight - e.pageY * 5) / 100;
        el.style.transform = "translateX(" + x + "px) translateY(" + y + "px)";
    });
});
