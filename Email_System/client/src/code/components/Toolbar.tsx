// React imports.
import React from "react";

// Material-UI imports.
import Button from "@material-ui/core/Button";
import NewContactIcon from "@material-ui/icons/ContactMail";
import NewMessageIcon from "@material-ui/icons/Email";
import NewFreshIcon from "@material-ui/icons/Refresh";
import path from "path";

/**
 * Toolbar.
 */
const Toolbar = ({ state }) => (

  <div>
    <Button variant="contained" color="primary" size="small" style={{ marginRight:10 }}
      onClick={ () => state.showComposeMessage("new") } >
      <NewMessageIcon style={{ marginRight:10 }} />New Message
    </Button>
    <Button variant="contained" color="primary" size="small" style={{ marginRight:10 }}
      onClick={ state.showAddContact } >
      <NewContactIcon style={{ marginRight:10 }} />New Contact
    </Button>

    <Button variant="contained" color="primary" size="small" style={{ marginRight:10 }}
          onClick={ () => window.location.reload() } >
      <NewFreshIcon style={{ marginRight:10 }} />Refresh
    </Button>
  </div>

);


export default Toolbar;
