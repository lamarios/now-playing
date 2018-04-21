import React from 'react';
import ActivityPluginSelection from './ActivityPluginSelection.jsx';
import ActivityList from './ActivityList.jsx';
import NowPlayingService from './NowPlayingService.jsx'
import PluginSettings from './PluginSettings.jsx';

export default class Settings extends React.Component {
    constructor() {
        super();

        this.state = {
            activities: [],
            nowPlayingPlugins: [],
            activityMapping: {},
            activityPlugins: [],
            mergedPlugins: []
        };


        this.service = new NowPlayingService();

        this.refresh = this.refresh.bind(this);
        this.refreshActivities = this.refreshActivities.bind(this);
        this.mergePlugins = this.mergePlugins.bind(this);
    }


    componentDidMount() {
        this.refresh();
    }


    /**
     * Refresh the list of activities
     */
    refresh() {
        this.refreshActivities();
        this.service.getNowPlayingPlugins()
            .then(nowPlaying => {
                this.service.getAvailableActivityPlugins()
                    .then(activities => {
                        this.setState({
                            activityPlugins: activities.data,
                            nowPlayingPlugins: nowPlaying.data
                        }, () => this.mergePlugins());
                    })
            });

        this.service.getActivityMapping()
            .then(res => {
                this.setState({activityMapping: res.data});
            })
    }

    refreshActivities() {
        this.setState({activities: []}, () => {
            this.service.getActivities()
                .then(res => {
                    this.setState({activities: res.data});
                });
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
            <div>
                <h1>Now playing</h1>
                <hr/>
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
                <PluginSettings plugins={this.state.mergedPlugins}/>

            </div>
        );
    }
}