import React from 'react';

import {API} from './NowPlayingService';

export default class NowPlaying extends React.Component {
    constructor() {
        super();

        this.getImage = this.getImage.bind(this);
        this.state = {timeout: null, image: null, webSocket: null, heartbeat: null};

        this.onMessage = this.onMessage.bind(this);
        this.heartbeat = this.heartbeat.bind(this);
        this.connect = this.connect.bind(this);
    }

    componentDidMount() {
        // this.getImage();
        this.connect();
    }

    componentWillUnmount() {
        // clearTimeout(this.state.timeout);
        clearTimeout(this.state.heartbeat);
    }


    /**
     * connects to the websocket
     */
    connect() {
        //clearing timeout if it's still running
        clearTimeout(this.state.heartbeat);
        console.log('Connecting to websocket.');
        const webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/ws");
        webSocket.onmessage = this.onMessage;
        webSocket.onclose = () => {
            console.log('disconnected');
            setTimeout(this.connect, 20000);
        }
        webSocket.onerror = () => {
            console.log('error');
            setTimeout(this.connect, 20000);
        }
        this.setState({webSocket});
        this.heartbeat();
    }

    /**
     * Keeps the connection alive
     */
    heartbeat() {
        var timeout = 20000;
        if (this.state.webSocket !== null && this.state.webSocket.readyState === this.state.webSocket.OPEN) {
            this.state.webSocket.send('ping');
            console.log('ping');
        }

        this.setState({heartbeat: setTimeout(this.heartbeat, timeout)})

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