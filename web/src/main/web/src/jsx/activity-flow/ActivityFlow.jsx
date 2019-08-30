import React from 'react';
import {Service} from '../NowPlayingService';
import Node from './Node';

export default class ActivityFlow extends React.Component {
    state = {
        flow: {}
    };

    componentDidMount() {
        this.refreshActivitiesAndNowPlaying();
    }

    refreshActivitiesAndNowPlaying = () => {
        Service.getAvailableActivityPlugins()
            .then(activities => Service.getNowPlayingPlugins()
                .then(nowPlaying => Service.getFlow()
                    .then(flow => this.setState({
                        activities, nowPlaying, flow
                    }))
                ))
    }

    flowChanged = (value) => {
        if (this.state.debounce) {
            clearTimeout(this.state.debounce);
        }
        console.log('flow changed', value);
        const debounce = setTimeout(() => {
            Service.saveFlow(value);
        }, 500);
        this.setState({flow: value, debounce: debounce});
    }

    render() {
        return (<div className="ActivityFlow">
            <Node activities={this.state.activities} nowPlaying={this.state.nowPlaying}
                  valueChanged={this.flowChanged} node={this.state.flow}/>
        </div>);
    }
}
