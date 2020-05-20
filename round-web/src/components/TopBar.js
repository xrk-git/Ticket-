import React from 'react';
import logo from '../assets/images/logo.svg';
import { Icon } from 'antd';

export class TopBar extends React.Component {
    render() {
        return (
            <header className="App-header">
                <img src={logo} className="App-logo" alt="logo" />
                <span className="App-title">Around</span>
                {this.props.isLoggedIn ?
                    // eslint-disable-next-line jsx-a11y/anchor-is-valid
                    <a className="logout" onClick={this.props.handleLogout}>
                        <Icon type="logout"/>{' '}Logout
                    </a> : null}
            </header>
        );
    }
}

export default TopBar;
