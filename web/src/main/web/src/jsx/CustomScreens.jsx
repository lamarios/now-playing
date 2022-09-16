import React from 'react';
import {Service} from './NowPlayingService.jsx';
import {API} from './NowPlayingService';

export default class CustomScreens extends React.Component {
    constructor() {
        super();

        this.state = {newName: '', newWidth: 0, newHeight: 0, screens: {}, plugins: [], variables: {}};
        this.addScreen = this.addScreen.bind(this);
        this.getScreens = this.getScreens.bind(this);
        this.getPlugins = this.getPlugins.bind(this);
        this.setVariable = this.setVariable.bind(this);
        this.saveVariables = this.saveVariables.bind(this);
        this.selectPlugin = this.selectPlugin.bind(this);
    }

    componentDidMount() {
        this.getScreens();
        this.getPlugins();
    }

    getPlugins() {
        Service.getScreensPlugins().then(r => this.setState({plugins: r}));
    }

    setVariable(variable, valueName, value) {

        const variables = this.state.variables;
        if (!variables[variable]) {
            variables[variable] = {};
        }

        variables[variable][valueName] = value;
        console.log(variables);
        this.setState({variables: variables});
    }

    saveVariables() {
        Service.saveVariables(this.state.selectedScreen.id, this.state.selectedPlugin.id, this.state.variables).then(r => alert('yay'));
    }

    addScreen() {
        console.log(this.state);
        try {
            if (this.state.newName.length > 0 && parseInt(this.state.newWidth) > 0 && parseInt(this.state.newHeight) > 0) {
                Service.addScreen({
                    id: this.state.newName,
                    width: this.state.newWidth,
                    height: this.state.newHeight
                }).then(result => {
                    alert('Screen added');
                    this.getScreens();
                })
            } else {
                alert('Invalid parameters, name must be a string, width and height must be numbers > 0')
            }
        } catch (e) {
            alert('Invalid parameters, name must be a string, width and height must be numbers > 0')
        }
    }

    getScreens() {
        Service.getScreens().then(res => this.setState({screens: res}));
    }

    selectPlugin(e) {
        console.log('hello');
        if (e.target.value.trim().length > 0) {
            let variables = {};
            const plugin = this.state.plugins.filter(p => p.id === e.target.value)[0];
            if (this.state.selectedScreen && this.state.selectedScreen.pluginTemplates[plugin.id]) {
                variables = this.state.selectedScreen.pluginTemplates[plugin.id];
            }

            console.log('plugin', plugin, 'variables', variables);
            this.setState({
                variables: variables,
                selectedPlugin: plugin
            });
        } else {
            this.setState({
                variables: {},
                selectedPlugin: null
            })
        }
    }

    render() {
        const screenIds = Object.keys(this.state.screens);
        return (<div>
            {screenIds.length > 0 && <div>
                <h3>Configure screens</h3>
                <select onChange={(e) => this.setState({
                    selectedPlugin: null,
                    selectedScreen: this.state.screens[e.target.value]
                })}>
                    <option>Select screen to configure</option>
                    {screenIds.map(s => <option key={s} value={s}>{s}</option>)}
                </select>
                {this.state.selectedScreen && <div>
                    <p>Width: {this.state.selectedScreen.width}px, Height: {this.state.selectedScreen.height}px</p>
                    <select
                        onChange={this.selectPlugin}>
                        <option>Select plugin to customize</option>
                        {this.state.plugins.map(p => <option value={p.id} key={p.id}>{p.name}</option>)}
                    </select>
                    {this.state.selectedPlugin && <div>
                        {this.state.selectedPlugin.variables.map(v => {

                            let variables = this.state.variables[v] || {};

                            return (<div key={v}>
                                <h4>{v}</h4>
                                x: <input type="test" defaultValue={variables.x || 0}
                                          onChange={e => this.setVariable(v, 'x', e.target.value)}/>
                                <br/>
                                y: <input type={"number"} defaultValue={variables.y || 0}
                                          onChange={e => this.setVariable(v, 'y', e.target.value)}/>
                                <br/>
                                width: <input type={"number"} defaultValue={variables.width || 0}
                                              onChange={e => this.setVariable(v, 'width', e.target.value)}/>
                                <br/>
                                height: <input type={"number"} defaultValue={variables.height || 0}
                                               onChange={e => this.setVariable(v, 'height', e.target.value)}/>
                                <br/>
                                <br/>
                            </div>)
                        })}
                        <button onClick={this.saveVariables}>save</button>
                    </div>}
                </div>}

            </div>}
            <div>
                <h3>Add screen</h3>
                <label>Name</label>
                <input type={"text"} defaultValue={""} onChange={(e) => this.setState({newName: e.target.value})}/>
                <br/>
                <label>Width (px)</label>
                <input type={"text"} defaultValue={0} onChange={(e) => this.setState({newWidth: e.target.value})}/>
                <br/>
                <label>Height (px)</label>
                <input type={"text"} defaultValue={0} onChange={(e) => this.setState({newHeight: e.target.value})}/>
                <br/>
                <button onClick={this.addScreen}>Add screen</button>
            </div>
        </div>);
    }

}
