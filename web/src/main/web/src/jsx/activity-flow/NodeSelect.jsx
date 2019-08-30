import React from 'react';
import {equal} from 'fast-deep-equal';

export default class NodeSelect extends React.Component {
    state = {};

    componentDidUpdate(prevProps, prevState, snapshot) {
    }

    valueSelected = (value) => {
        console.log('select value changed');
        this.setState({selected: value});
        this.props.onChange(value);
    }

    render() {
        return (<></>);
    }

}
