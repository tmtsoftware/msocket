

console.log("hello")

const webSocket = new WebSocket("ws://localhost:5000/websocket-endpoint");
webSocket.onmessage = (m) => {
    console.log(m)
};
webSocket.onopen = () => {
    console.log("onOpen")
    webSocket.send("{\"Hello\":{\"name\":\"msuhtaq\"}}")
    webSocket.send("{\"Square\":{\"number\":9}}")
    webSocket.send("{\"GetNumbers\":{\"divisibleBy\":3}}")
};
webSocket.onclose = () => console.log("onClose");
webSocket.onerror = () => console.log("errror");

