document.addEventListener('DOMContentLoaded', function () {
    bindEnterKeyToSaveButtons();
});


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
    console.log("Due date is " + dueDate);
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
    const saveButton = document.querySelector('#add-item-modal .save-button');
    saveButton.setAttribute('data-url', `/list/${listName}/item`);
    $('#add-item-modal').modal('show');
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

function editItem(itemId, title, description, dueDate) {
    document.getElementById('editTitle').value = title;
    document.getElementById('editDescription').value = description;
    document.getElementById('editDueDate').value = dueDate;
    document.getElementById('editItemId').value = itemId;

    const listName = document.querySelector('h1').innerText;
    const saveButton = document.querySelector('#edit-item-modal .save-button');
    saveButton.setAttribute('data-url', `/list/${listName}/item/${itemId}`);

    $('#edit-item-modal').modal('show');
}

function bindEnterKeyToSaveButtons() {
    document.querySelectorAll('.modal').forEach(modalEl => {
        const saveBtn = modalEl.querySelector('.save-button');
        let enterHandler = null;

        modalEl.addEventListener('shown.bs.modal', () => {
            enterHandler = (e) => {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    saveBtn.click();
                }
            };
            modalEl.addEventListener('keydown', enterHandler);
        });

        modalEl.addEventListener('hidden.bs.modal', () => {
            if (enterHandler) {
                modalEl.removeEventListener('keydown', enterHandler);
                enterHandler = null;
            }
        });
    });
}