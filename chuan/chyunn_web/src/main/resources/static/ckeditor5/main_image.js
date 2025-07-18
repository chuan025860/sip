import {
	ClassicEditor,
	Autoformat,
	Autosave,
	BlockQuote,
	Bold,
	Essentials,
	FontBackgroundColor,
	FontColor,
	FontFamily,
	FontSize,
	Heading,
	Indent,
	IndentBlock,
	Italic,
	Link,
	List,
	ListProperties,
	Paragraph,
	TextTransformation,
	TodoList,
	Underline,
	Image,
	ImageToolbar,
	ImageUpload,
	ImageStyle,
	ImageResize,
	ImageCaption,
	// Base64UploadAdapter,
} from 'ckeditor5';

/**
 * Create a free account with a trial: https://portal.ckeditor.com/checkout?plan=free
 */
const LICENSE_KEY = 'GPL'; // or <YOUR_LICENSE_KEY>.

const editorConfig = {
	language: 'zh',
	toolbar: {
		items: [
			'heading',
			'|',
			'fontSize',
			'fontFamily',
			'fontColor',
			'fontBackgroundColor',
			'|',
			'bold',
			'italic',
			'underline',
			'|',
			'link',
			'blockQuote',
			'|',
			'link', 'blockQuote', 'imageUpload', // ← 加入 imageUpload
			'|',
			'bulletedList',
			'numberedList',
			'todoList',
			'outdent',
			'indent'
		],
		shouldNotGroupWhenFull: false
	},
	plugins: [
		Autoformat,
		Autosave,
		BlockQuote,
		Bold,
		Essentials,
		FontBackgroundColor,
		FontColor,
		FontFamily,
		FontSize,
		Heading,
		Indent,
		IndentBlock,
		Italic,
		Link,
		List,
		ListProperties,
		Paragraph,
		TextTransformation,
		TodoList,
		Underline,
		Image,
		ImageToolbar,
		ImageUpload,
		ImageStyle,
		ImageCaption,
		ImageResize,
		// Base64UploadAdapter
	],
	fontFamily: {
		supportAllValues: true
	},
	fontSize: {
		options: [10, 12, 14, 'default', 18, 20, 22],
		supportAllValues: true
	},
	heading: {
		options: [
			{
				model: 'paragraph',
				title: 'Paragraph',
				class: 'ck-heading_paragraph'
			},
			{
				model: 'heading1',
				view: 'h1',
				title: 'Heading 1',
				class: 'ck-heading_heading1'
			},
			{
				model: 'heading2',
				view: 'h2',
				title: 'Heading 2',
				class: 'ck-heading_heading2'
			},
			{
				model: 'heading3',
				view: 'h3',
				title: 'Heading 3',
				class: 'ck-heading_heading3'
			},
			{
				model: 'heading4',
				view: 'h4',
				title: 'Heading 4',
				class: 'ck-heading_heading4'
			},
			{
				model: 'heading5',
				view: 'h5',
				title: 'Heading 5',
				class: 'ck-heading_heading5'
			},
			{
				model: 'heading6',
				view: 'h6',
				title: 'Heading 6',
				class: 'ck-heading_heading6'
			}
		]
	},
	initialData: '',
	licenseKey: LICENSE_KEY,
	link: {
		addTargetToExternalLinks: true,
		defaultProtocol: 'https://',
		decorators: {
			toggleDownloadable: {
				mode: 'manual',
				label: 'Downloadable',
				attributes: {
					download: 'file'
				}
			}
		}
	},
	image: {
		toolbar: [
			'imageStyle:inline',
			'imageStyle:block',
			'imageStyle:side',
			'|',
			'toggleImageCaption',
			'imageTextAlternative'
		],
		styles: [
			'inline',
			'block',
			'side'
		],
		resizeUnit: 'px'
	},
	list: {
		properties: {
			styles: true,
			startIndex: true,
			reversed: true
		}
	},
	placeholder: ''
};
function MyUploadAdapterPlugin(editor) {
	editor.plugins.get('FileRepository').createUploadAdapter = (loader) => {
		return new MyUploadAdapter(loader);
	};
}

class MyUploadAdapter {
	constructor(loader) {
		this.loader = loader;
	}

	upload() {
		return this.loader.file.then(file => {
			console.log("上傳觸發：", file); // 檢查這個有沒有印出
			return new Promise((resolve, reject) => {
				const data = new FormData();
				data.append('upload', file);

				$.ajax({
					url: '/chyunn/resource_manage/image', // Spring Boot 上傳 API
					type: 'POST',
					data: data,
					processData: false,
					contentType: false,
					success: res => {
						console.log("ok")
						resolve({ default: res.url }); // 這裡的 default 是關鍵！
					},
					error: err => {
						console.log("error")
						reject('上傳失敗');
					}
				});
			});
		});
	}
}

ClassicEditor
	.create(document.querySelector('#eventContent'), {
		...editorConfig,
		extraPlugins: [MyUploadAdapterPlugin]
	})
	.then(editor => {
		window.editorInstance = editor;
		console.log('✅ CKEditor 啟動成功');

		editor.editing.view.change(writer => {
			writer.setStyle('font-weight', 'normal', editor.editing.view.document.getRoot());
		});

		let content = document.querySelector("#eventContent").value || "";
		editor.setData(content);
	})
	.catch(error => {
		console.error('❌ CKEditor 加載失敗:', error);
	});
