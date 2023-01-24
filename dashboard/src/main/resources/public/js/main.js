function toast(message, displayLength = 4000) {
    M.toast({
        html: message,
        displayLength,
    });
}

// We had to rename this form _ to id because
// the fucking patreon button has lodash set on the window
function id(el) {
    return document.getElementById(el);
}

function hide(itemId) {
    id(itemId).style.display = 'none';
}

function unHide(itemId) {
    id(itemId).style.display = 'block';
}

document.addEventListener('DOMContentLoaded', () => {
    id('year').innerHTML = `${(new Date()).getFullYear()}`;
});

function getMessage(m) {
    switch (m) {
        case 'missing_input':
            return 'Please fill in all fields';
        case 'no_user':
            return 'The specified user id did not resolve any users.';
        case 'no_guild':
            return 'The specified server id did not resolve any servers.';
        default:
            return m;
    }
}

