import React from 'react';
import RequestNode from "./RequestNode";
import ActivityNode from "./ActivityNode";

export default class Node extends React.Component {
    mounted = true;

    state = {
        nodeValue: {}
    };

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.mounted && prevProps.node !== this.props.node) {
            console.log('props changed', prevProps.node, this.props.node);
            this.setFromProps();
        }
    }

    componentDidMount() {
        this.setFromProps();
    }

    componentWillUnmount() {
        this.mounted = false;
    }


    /**
     * We need to reconstruct the state based on the props given from parent nodes
     * this is when the user loads the page
     */
    setFromProps = () => {
        const node = this.props.node;
        if (node) {

            let type = '';
            let value = '';
            if (node.request) {
                type = 'request';
                value = node.request.type;
            } else if (node.activity) {
                type = 'activity';
                value = node.activity.plugin;
            } else if (node.nowPlaying) {
                type = 'nowPlaying';
                value = node.nowPlaying;
            }

            const selected = type + "|" + value;
            if (value && value.length > 0) {
                this.setState({selected: selected, nodeValue: this.props.node, type: type, value: value});
            }
            ;
        }
    }


    nodeSelected = (value) => {
        const split = value.split("|")
        const current = this.state.nodeValue;

        const selectValue = split[1];

        let nodeValue = {};
        const type = split[0];
        if (type === 'nowPlaying') {
            if (!current.nowPlaying || (current.nowPlaying !== selectValue)) {
                nodeValue = {
                    nowPlaying: selectValue
                };
            }
        } else if (type === 'activity') {
            if (!current.activity || (current.activity.plugin !== selectValue)) {
                nodeValue = {
                    activity: {
                        plugin: selectValue
                    }
                }
            }
        }

        this.setState({type: type, value: selectValue, nodeValue: nodeValue}, this.nodeValueChanged);
    }

    nodeValueChanged = () => {
        // we send our value to the parent node
        this.props.valueChanged(this.state.nodeValue);
    }

    activityNodeChanged = (node) => {
        this.setState({
            nodeValue: {
                activity: node
            }
        }, this.nodeValueChanged);
    }

    requestNodeChanged = (node) => {
        this.setState({nodeValue: {request: node}}, this.nodeValueChanged);
    }

    render() {
        const activities = this.props.activities;
        const nowPlaying = this.props.nowPlaying;
        const type = this.state.type;
        const value = this.state.value;
        const node = this.props.node;
        return (<div className="Node">
            <select className="NodeSelect" onChange={(e) => this.nodeSelected(e.target.value)}
                    value={this.state.selected}>
                <optgroup label="Request Parameters">
                    <option value="request|queryParam">Query parameter filter</option>
                </optgroup>
                <optgroup label="Activity Plugin">
                    {activities && activities.map(a => <option key={a.id} value={'activity|' + a.id}>{a.name}</option>)}
                </optgroup>
                <optgroup label="Now playing Plugin">
                    {nowPlaying && nowPlaying.map(a => <option key={a.id}
                                                               value={'nowPlaying|' + a.id}>{a.name}</option>)}
                </optgroup>
            </select>

            {type && type === 'request' &&
            <RequestNode activities={this.props.activities} nowPlaying={this.props.nowPlaying}
                         valueChanged={this.requestNodeChanged} node={node && node.request ? node.request : null}/>}
            {type && type === 'activity' &&
            <ActivityNode plugin={value} activities={this.props.activities} nowPlaying={this.props.nowPlaying}
                          valueChanged={this.activityNodeChanged} node={node && node.activity ? node.activity : null}/>}
        </div>);
    }

}
