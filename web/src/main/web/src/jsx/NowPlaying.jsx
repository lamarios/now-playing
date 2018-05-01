import React from 'react';


export default class NowPlaying extends React.Component {
    constructor() {
        super();

        this.getImage = this.getImage.bind(this);
        this.state = {timeout: null, image: null, webSocket: null};

        this.onMessage = this.onMessage.bind(this);
    }

    componentDidMount() {
        // this.getImage();
        const webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/ws");
        webSocket.onmessage = this.onMessage;

        this.setState({webSocket});
    }

    componentWillUnmount() {
        // clearTimeout(this.state.timeout);

    }


    /**
     * Upon receiving websocket messages
     * @param message
     */
    onMessage(message) {
        console.log('websocket message', message);
        if (message.data === 'refresh') {
            this.getImage();
        }
    }


    getImage() {
        const width = window.innerWidth;
        const height = window.innerHeight;
        const scale = window.devicePixelRatio;

        console.log('loading');
        var img = new Image();
        img.onload = () => {
            console.log('loaded');
        };

        const url = API.NOW_PLAYING.GET_IMAGE.format(width, height, scale) + "&" + new Date().getTime();
        img.src = url;
        this.setState({image: img});

    }

    render() {
        // const style = {
        //     backgroundImage: this.state.image !== null ? this.state.image : ''
        // };
        return (<div className="NowPlaying">
            {this.state.image !== null && <img src={this.state.image.src}/>}
        </div>)

    };
}