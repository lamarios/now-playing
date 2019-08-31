import React from 'react';
import RequestNode from "./RequestNode";
import ActivityNode from "./ActivityNode";

export default class Node extends React.Component {
    mounted = true;

    state = {};

    componentDidUpdate(prevProps, prevState, snapshot) {
    }

    componentDidMount() {
    }

    componentWillUnmount() {
        this.mounted = false;
    }


    getNode = () => {
        let node = this.props.node;
        if (!node) {
            node = {}
        }

        return node;
    }

    // /**
    //  * We need to reconstruct the state based on the props given from parent nodes
    //  * this is when the user loads the page
    //  */
    // setFromProps = () => {
    //     const node = this.props.node;
    //     if (node) {
    //
    //         let type = '';
    //         let value = '';
    //         if (node.request) {
    //             type = 'request';
    //             value = node.request.type;
    //         } else if (node.activity) {
    //             type = 'activity';
    //             value = node.activity.plugin;
    //         } else if (node.nowPlaying) {
    //             type = 'nowPlaying';
    //             value = node.nowPlaying;
    //         }
    //
    //         const selected = type + "|" + value;
    //         if (value && value.length > 0) {
    //             this.setState({selected: selected, nodeValue: this.props.node, type: type, value: value});
    //         }
    //         ;
    //     }
    // }


    nodeSelected = (value) => {
        const split = value.split("|")

        const selectValue = split[1];

        let node = this.getNode();
        const type = split[0];
        if (type === 'nowPlaying') {
            if (!node.nowPlaying || (node.nowPlaying !== selectValue)) {
                node = {
                    nowPlaying: selectValue
                };
            }
        } else if (type === 'activity') {
            if (!node.activity || (node.activity.plugin !== selectValue)) {
                node = {
                    activity: {
                        plugin: selectValue
                    }
                }
            }
        } else if (type === 'request') {
            if (!node.request || (node.request.type !== selectValue)) {
                node = {
                    request: {
                        type: selectValue
                    }
                }
            }
        }


        this.props.valueChanged(node);
    }


    activityNodeChanged = (node) => {
        this.props.valueChanged({
            activity: node
        });
    }

    requestNodeChanged = (node) => {
        this.props.valueChanged({
            request: node
        });
    }

    render() {
        const activities = this.props.activities;
        const nowPlaying = this.props.nowPlaying;
        const node = this.getNode();
        const type = node.request ? "request" : node.activity ? "activity" : "nowPlaying";
        const selectValue = node.request ? node.request.type : node.activity ? node.activity.plugin : node.nowPlaying ? node.nowPlaying : "";
        const final = node && node.nowPlaying;
        return (<div className={"Node " + (final ? "final" : "")}>
            <select className="NodeSelect" onChange={(e) => this.nodeSelected(e.target.value)}
                    value={type + "|" + selectValue}>
                <option value="">Select</option>
                <optgroup label="Client request filter">
                    <option value="request|queryParam">Query parameter filter</option>
                </optgroup>
                <optgroup label="Activity plugin">
                    {activities && activities.map(a => <option key={a.id}
                                                               value={'activity|' + a.id}>{a.name}</option>)}
                </optgroup>
                <optgroup label="Display">
                    {nowPlaying && nowPlaying.map(a => <option key={a.id}
                                                               value={'nowPlaying|' + a.id}>{a.name}</option>)}
                </optgroup>
            </select>

            {type && type === 'request' &&
            <RequestNode activities={this.props.activities} nowPlaying={this.props.nowPlaying}
                         valueChanged={this.requestNodeChanged} node={node && node.request ? node.request : null}/>}
            {type && type === 'activity' &&
            <ActivityNode plugin={node.activity.plugin} activities={this.props.activities}
                          nowPlaying={this.props.nowPlaying}
                          valueChanged={this.activityNodeChanged}
                          node={node && node.activity ? node.activity : null}/>}
        </div>);
    }

}
