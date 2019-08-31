import React from 'react';
import ActivityPluginSelection from './ActivityPluginSelection.jsx';
import ActivityList from './ActivityList.jsx';
import {Service}from './NowPlayingService.jsx'
import PluginSettings from './PluginSettings.jsx';
import ActivityFlow from "./activity-flow/ActivityFlow";

export default class Settings extends React.Component {
    constructor() {
        super();

        this.state = {
            activities: [],
            nowPlayingPlugins: [],
            activityPlugins: [],
            mergedPlugins: []
        };



        this.refresh = this.refresh.bind(this);
        this.mergePlugins = this.mergePlugins.bind(this);
    }


    componentDidMount() {
        this.refresh();
    }


    /**
     * Refresh the list of activities
     */
    refresh() {
        Service.getNowPlayingPlugins()
            .then(nowPlaying => {
                Service.getAvailableActivityPlugins()
                    .then(activities => {
                        this.setState({
                            activityPlugins: activities,
                            nowPlayingPlugins: nowPlaying
                        }, () => this.mergePlugins());
                    })
            });

    }


    mergePlugins() {
        var merged = [];

        this.state.activityPlugins.forEach((plugin, index) => {
            merged.push(plugin);
        });

        this.state.nowPlayingPlugins.forEach((plugin, index) => {
            var found = false;

            //checking if not already in our list
            merged.forEach((p, i) => {
                if (p.id === plugin.id) {
                    found = true;
                }
            });


            if (!found) {
                merged.push(plugin);
            }
        });

        console.log(merged);
        this.setState({mergedPlugins: merged});

    }

    render() {
        return (
            <div className="Settings">
                <h1>Now playing</h1>
                <div className="disclaimer">
                    Please note that Now Playing is not meant to be exposed outside of your local network. Password are stored in clear and there is not authentication to protect from any attack.
                </div>
                <div>
                    You can access the now playing screen via the following options:
                    <ul>
                        <li>As an auto refreshing webpage: <a href={location.origin+'/now-playing'}>{location.origin}/now-playing</a>. <small><em>Will auto detect width, height and scale</em></small></li>
                        <li>As an image <a href={location.origin+'/now-playing.jpg'}>{location.origin}/now-playing.jpg?width=&lt;desiredWidth&gt;&amp;height=&lt;desiredHeight&gt;&amp;scale=&lt;pixelRatio&gt;</a>. <small><em>Defaults to 1920*1080 with a scale of 1. For a high DPI screen, you need to change the pixel ratio i.e for an iPad width: 1024, height:768, scale: 2</em></small></li>
                    </ul>
                </div>

                <h2>Flow</h2>

                <p>Here you can set up the flow on how the image should show on your clients</p>
                <ActivityFlow />
{/*
                <ActivityPluginSelection onActivityChange={() => this.refresh()} plugins={this.state.activityPlugins}/>
                <ActivityList
                    activities={this.state.activities}
                    plugins={this.state.nowPlayingPlugins}
                    mapping={this.state.activityMapping}
                    refreshActivities={this.refreshActivities}
                    onMappingChanged={(activity, plugin) => {
                        var state = this.state.activityMapping;
                        state[activity] = plugin;
                        this.setState({activityMapping: state});

                    }}
                />
*/}
                <PluginSettings plugins={this.state.mergedPlugins}/>

            </div>
        );
    }
}