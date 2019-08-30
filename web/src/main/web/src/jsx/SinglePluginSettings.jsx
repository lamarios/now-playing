import React from 'react';
import {Service} from './NowPlayingService.jsx';

export default class SinglePluginSettings extends React.Component {
    constructor(props) {
        super(props);

        this.state = {values: {}, errors: []};

        this.onSettingChange = this.onSettingChange.bind(this);
        this.saveSettings = this.saveSettings.bind(this);
    }

    componentDidMount() {
        if (this.props.values !== undefined) {
            this.setState({values: this.props.values})
        }
    }

    saveSettings() {
        console.log('saving');
        var values = this.state.values;
        values.pluginId = this.props.plugin;
        Service.saveSetting(values)
            .then(res => {
                this.setState({errors: res});
            });
    }

    onSettingChange(e) {
        var value = this.state.values;

        value[e.target.name] = e.target.value;
        this.setState({values: value});
        console.log('new setting', value);

    }


    render() {
        return (<div className={"SinglePluginSettings"}>
                {this.props.settings !== undefined && this.props.settings.length > 0 &&
                <div>
                    {this.state.errors.length > 0 &&
                    <div className="errors">
                        {this.state.errors.map((error, i) => (
                            <p key={i}>{error}</p>
                        ))}
                    </div>}


                    {this.props.settings.map((s, i) => {
                        var defaultValue = "";
                        if (this.props.values !== undefined && this.props.values[s.name] !== undefined) {
                            defaultValue = this.props.values[s.name];
                        }
                        switch (s.type) {
                            case 'PASSWORD':
                                return (<label key={s.name}>
                                    {s.label}:
                                    <input type="password" onChange={this.onSettingChange} name={s.name}
                                           defaultValue={defaultValue}/>
                                </label>);
                                break;
                            case 'TEXT':
                                return (<label key={s.name}>
                                    {s.label}:
                                    <input type="text" onChange={this.onSettingChange} name={s.name}
                                           defaultValue={defaultValue}/>
                                </label>);
                                break;
                        }

                    })}

                    <button onClick={this.saveSettings}>Save</button>
                </div>}
            </div>
        );
    }
}