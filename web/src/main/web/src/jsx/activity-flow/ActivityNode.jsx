import React from 'react';
import {Service} from "../NowPlayingService";
import Node from "./Node";


export default class ActivityNode extends React.Component {
    state = {};

    componentDidMount() {
        this.getActivities();
    }

    getNode = () => {
        let node = this.props.node;
        if (!node) {
            node = {
                plugin: '',
                nodes: {}
            }
        }

        if (!node.nodes) {
            node.nodes = {};
        }

        if (!node.plugin) {
            node.plugin = '';
        }

        console.log('activity node', this.props.node);
        return node;
    }


    componentDidUpdate(prevProps, prevState, snapshot) {
        if (prevProps.plugin !== this.props.plugin) {
            this.getActivities();
        }
    }

    getActivities = () => {
        Service.getActivitiesForPlugin(this.props.plugin)
            .then(activities => this.setState({activities}))
    };

    activityChanged = (activtyId, childNodeValue) => {
        let node = this.getNode();
        console.log('activity changed',node, activtyId, childNodeValue);
        node.nodes[activtyId] = childNodeValue;

        this.props.valueChanged(node);

    }

    render() {
        const activities = this.state.activities;
        const node = this.getNode();

        return (<div className="ActivityNode">
            {activities && activities.map(a => {
                const activityNode =  node.nodes[a.id];
                return <div key={a.id}>
                    {a.name}: <Node activities={this.props.activities} nowPlaying={this.props.nowPlaying}
                                    valueChanged={(nodeValue) => this.activityChanged(a.id, nodeValue)}
                                    node={activityNode}/>
                </div>
            })}
        </div>);
    }
}

