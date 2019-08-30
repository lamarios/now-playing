import React from 'react';
import  {Service} from  './NowPlayingService.jsx'


export default class ActivityList extends React.Component {
    constructor(props) {
        super(props);
        this.saveMapping = this.saveMapping.bind(this);
    }


    /**
     * Saves an activity / plugin association
     */
    saveMapping(event) {
        var activity = event.target.name;
        var plugin = event.target.value;
        this.props.onMappingChanged(activity, plugin);
        Service.setMapping(activity, plugin);
    }

    render() {
        return (<div className='ActivityList'>
                <p>Assign available activities to a Now Playing plugin</p>
                <button onClick={this.props.refreshActivities}>Refresh activities</button>
                <table>
                    <thead>
                    <tr>
                        <th>Activity</th>
                        <th>Now playing plugin</th>
                    </tr>
                    </thead>
                    <tbody>
                    {this.props.activities.map((activity, index) => {
                        return (<tr key={activity.id}>
                                <td>{activity.name}</td>
                                <td>
                                    <select name={activity.id}
                                            value={this.props.mapping[activity.id]}
                                            onChange={this.saveMapping}>
                                        {this.props.plugins.map((plugin, i) => {
                                            return (<option key={plugin.id} value={plugin.id}>{plugin.name}</option>);
                                        })}

                                    </select>
                                </td>
                            </tr>
                        )
                    })}
                    </tbody>
                </table>
            </div>
        );
    };
}