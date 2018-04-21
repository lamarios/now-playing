import React from 'react';


export default class NowPlaying extends React.Component {
    constructor() {
        super();

        this.getImage = this.getImage.bind(this);
        this.state = {timeout: null, image: null};

    }

    componentDidMount() {
        this.getImage();
    }

    componentWillUnmount() {
        clearTimeout(this.state.timeout);
    }

    getImage() {
        const width = window.innerWidth;
        const height = window.innerHeight;
        const scale = window.devicePixelRatio;

        console.log('loading');
        var img = new Image();
        img.onload = () => {
            console.log('loaded');
            this.setState({image: img, timeout: setTimeout(() => this.getImage(), 5000)});
        };

        const url = API.NOW_PLAYING.GET_IMAGE.format(width, height, scale) + "&" + new Date().getTime();
        img.src = url;

    }

    render() {
        // const style = {
        //     backgroundImage: this.state.image !== null ? this.state.image : ''
        // };
        return (<div className="NowPlaying">
            {this.state.image !== null && <img src={this.state.image.src} />}
        </div>)

    };
}