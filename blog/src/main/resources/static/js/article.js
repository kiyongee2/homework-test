// 삭제 기능
const deleteButton = document.getElementById('delete-btn');

if (deleteButton) {
    deleteButton.addEventListener('click', event => {
        let id = document.getElementById('article-id').value;
        fetch(`/api/articles/${id}`, {
            method: 'DELETE'
        })
        .then(() => {
            alert('삭제가 완료되었습니다.');
            location.replace('/articles');
        });
    });
}

// 수정 기능
const modifyButton = document.getElementById('modify-btn');

if (modifyButton) {
    modifyButton.addEventListener('click', event => {
        let params = new URLSearchParams(location.search);
        let id = params.get('id');

        fetch(`/api/articles/${id}`, {
            method: 'PUT',
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                title: document.getElementById('title').value,
                content: document.getElementById('content').value
            })
        })
        .then(() => {
            alert('수정이 완료되었습니다.');
            location.replace(`/articles/${id}`);
        });
    });
}

// 등록 기능
const createButton = document.getElementById('create-btn');

if (createButton) {
    createButton.addEventListener('click', event => {
        fetch('/api/articles', {
            method: 'POST',
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                title: document.getElementById('title').value,
                content: document.getElementById('content').value,
                imageUrl: document.getElementById('image-url').value
            })
        })
        .then(() => {
            alert('등록 완료되었습니다.');
            location.replace('/articles');
        });
    });
}

// ===== AI 글쓰기 도우미 =====
const aiAssistButton = document.getElementById('ai-assist-btn');
const getSuggestionButton = document.getElementById('get-suggestion-btn');
const suggestionContent = document.getElementById('ai-suggestion-content');

// 모달 인스턴스 (요소가 있을 때만 생성)
const aiModalElement = document.getElementById('aiAssistModal');
const aiModal = aiModalElement ? new bootstrap.Modal(aiModalElement) : null;

// 제안을 현재 내용(content) 끝에 추가하는 함수
function appendSuggestionToContent(suggestion) {
    const contentArea = document.getElementById('content');
    if (contentArea.value.trim() === '') {
        contentArea.value = suggestion;
    } else {
        contentArea.value = contentArea.value.trimEnd() + '\n' + suggestion;
    }
}

// 모달 열기
if (aiAssistButton && aiModal) {
    aiAssistButton.addEventListener('click', event => {
        // 모달을 열 때 이전 입력/결과 초기화
        document.getElementById('ai-question').value = '';
        document.getElementById('ai-suggestion').style.display = 'none';
        suggestionContent.innerHTML = '';

        aiModal.show();
    });
}

// AI 제안 받기 - 백엔드(/api/ai-suggestions) 호출
if (getSuggestionButton) {
    getSuggestionButton.addEventListener('click', event => {
        const loading = document.getElementById('ai-loading');
        const suggestionBox = document.getElementById('ai-suggestion');

        loading.style.display = 'block';
        suggestionBox.style.display = 'none';
        suggestionContent.innerHTML = '';

        fetch('/api/ai-suggestions', {
            method: 'POST',
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                title: document.getElementById('title').value,
                content: document.getElementById('content').value,
                question: document.getElementById('ai-question').value
            })
        })
        .then(response => response.json())
        .then(data => {
            loading.style.display = 'none';

            const suggestions = data.suggestions || [];

            if (suggestions.length === 0) {
                suggestionContent.innerHTML =
                    '<p class="text-muted mb-0">받은 제안이 없습니다.</p>';
            } else {
                // 클릭 가능한 목록으로 표시
                const listGroup = document.createElement('div');
                listGroup.className = 'list-group';

                suggestions.forEach(text => {
                    const item = document.createElement('button');
                    item.type = 'button';
                    item.className = 'list-group-item list-group-item-action';
                    item.textContent = text;
                    // 제안을 클릭하면 현재 내용 끝에 추가하고 모달을 닫음
                    item.addEventListener('click', () => {
                        appendSuggestionToContent(text);
                        if (aiModal) {
                            aiModal.hide();
                        }
                    });
                    listGroup.appendChild(item);
                });

                suggestionContent.appendChild(listGroup);
            }

            suggestionBox.style.display = 'block';
        })
        .catch(error => {
            loading.style.display = 'none';
            suggestionContent.innerHTML =
                '<div class="alert alert-danger mb-0">제안을 가져오지 못했습니다. 잠시 후 다시 시도해 주세요.</div>';
            suggestionBox.style.display = 'block';
        });
    });
}

// 페이지 로드시 기존 이미지 표시
window.addEventListener('DOMContentLoaded', () => {
    const imageUrl = document.getElementById('image-url')?.value;
    if(imageUrl){
        displayImagePreview(imageUrl);
    }
});

// id가 image-upload인 엘리먼트
const imageUpload = document.getElementById('image-upload');
if(imageUpload){
    // 클릭 이벤트가 감지되면 이미지 업로드 요청 처리
    imageUpload.addEventListener('change', async (event) => {
        const file = event.target.files[0];
        if(!file) return;

        // 이미지 파일 검증
        if(!file.type.startsWith('image/')){
            alert('이미지 파일만 업로드 가능합니다.');
            return;
        }

        // 이미지 업로드 요청
        const formData = new FormData();
        formData.append('file', file);

        fetch('/api/upload', {
            method: 'POST',
            body: formData
        }).then((response) => {
            if(!response.ok){
                alert('이미지 업로드에 실패했습니다.');
                throw new Error();
            }
            return response.json();
        }).then((data) => {
            document.getElementById('image-url').value = data.imageUrl;
            displayImagePreview(data.imageUrl);
        }).catch((e) => console.error(e));
    });
}

// 이미지 미리보기를 위한 함수
function displayImagePreview(imageUrl){
    const preview = document.getElementById('image-preview');
    const previewImg = document.getElementById('preview-img');

    if(preview && previewImg && imageUrl){
        previewImg.src = imageUrl;
        preview.style.display = 'block';
    }
}

// id가 remove-image-btn인 엘리먼트
const removeImageButton = document.getElementById('remove-image-btn');
if(removeImageButton){
    removeImageButton.addEventListener('click', () => {
        document.getElementById('image-url').value = '';
        document.getElementById('image-upload').value = '';
        document.getElementById('image-preview').style.display = 'none';
    })
}

