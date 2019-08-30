import React from 'react';
import {Service} from './NowPlayingService.jsx';

export default class ActivityPluginSelection extends React.Component {

    constructor(props) {
        super(props);

        this.state = {selected: ''};

        this.refresh = this.refresh.bind(this);
        this.activityChanged = this.activityChanged.bind(this);
    }

    componentDidMount() {
        this.refresh();
    }

    /**
     * REfreshes the available data
     */
    refresh() {
        Service.getCurrentActivityPlugin()
            .then(current =>{
                this.setState({selected: current});
            });
    }


    /**
     * When the activity changes, we need to send the data to the backend
     * @param event
     */
    activityChanged(event) {
        this.setState({selected: event.target.value}, () => {
            Service.setActivityPlugin(this.state.selected)
                .then(res => {
                    this.props.onActivityChange();
                });
        });


    }

    render() {
        return (<div className="ActivityPluginSelection">
            Select activity plugin:
            <select value={this.state.selected} onChange={this.activityChanged}>
                {this.props.plugins.map((v, k) => {
                    return (
                        <option
                            key={v.id}
                            value={v.id}
                        >{v.name}</option>
                    )
                })}
            </select>
        </div>);
    }
}