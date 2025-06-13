function sortList(listItemKey) {
    const listName = document.querySelector('h1').innerText;

}

function completeItem(checkbox) {
    console.log('completeItem function called');
    const url = checkbox.getAttribute('data-url');
    console.log('url: ' + url);
    const completed = checkbox.checked;
    fetch(`${url}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ completed: completed })
    }).then(response => {
        if (response.ok) {
            location.reload();
        }
    });
}

function updateItem(itemId, title, description, dueDate, url) {
    const updatedItem = {
        title: title,
        description: description,
        dueDate: dueDate
    };

    fetch(url, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(updatedItem)
    }).then(response => {
        if (response.ok) {
            location.reload();
        } else {
            console.error('Failed to update item');
        }
    });
}

function openAddItemModal() {
    const listName = document.querySelector('h1').innerText;
    const saveButton = document.querySelector('#addItemModal .btn-primary');
    saveButton.setAttribute('data-url', `/list/${listName}/item`);
    $('#addItemModal').modal('show');
}

function addItem(title, description, dueDate, url) {
    const item = {
        title: title,
        description: description,
        dueDate: dueDate
    };

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(item)
    }).then(response => {
        if (response.ok) {
            location.reload();
        } else {
            console.error('Failed to add item');
        }
    });
}


function deleteItem(button) {
    console.log('deleteItem function called');
    const url = button.getAttribute('data-url');
    console.log('url: ' + url);
    fetch(`${url}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        }
    }).then(response => {
        if (response.ok) {
            location.reload();
        }
    });
}

function parseDate(dateString) {
    const date = new Date(dateString);
    const userTimezoneOffset = date.getTimezoneOffset() * 60000;
    const adjustedDate = new Date(date.getTime() - userTimezoneOffset);
    return adjustedDate.toISOString().split('T')[0];
}

function editItem(itemId, title, description, dueDate) {
    document.getElementById('editTitle').value = title;
    document.getElementById('editDescription').value = description;
    document.getElementById('editDueDate').value = parseDate(dueDate);
    document.getElementById('editItemId').value = itemId;

    const listName = document.querySelector('h1').innerText;
    const saveButton = document.querySelector('#editItemModal .btn-primary');
    saveButton.setAttribute('data-url', `/list/${listName}/item/${itemId}`);

    $('#editItemModal').modal('show');
}